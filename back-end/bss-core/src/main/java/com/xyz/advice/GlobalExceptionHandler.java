package com.xyz.advice;

import com.xyz.common.HttpStatusEnum;
import com.xyz.common.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.lang.Nullable;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.AccountStatusException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>Package Name: com.xyz.advice</p>
 * <p>Description: 全局异常处理器（统一返回 ResponseResult + HttpStatusEnum）。</p>
 * <p>Coverage:
 * 参数校验 / 绑定、JSON 解析、方法/媒体类型错误、404/405、
 * 认证认证 / 授权、安全相关、数据库约束 (Oracle ORA)、上传大小、以及兜底异常。
 * </p>
 * <p>Create Time: 2025/9/4</p>
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since 21
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================ 参数绑定 & 校验 ============================

    /** @Valid 校验失败（JSON -> DTO） */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = buildFieldErrorMessage(e.getBindingResult());
        log.warn("[400] MethodArgumentNotValidException: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    /** 表单/QueryString 绑定失败 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseResult<Void> handleBindException(BindException e) {
        String msg = buildFieldErrorMessage(e.getBindingResult());
        log.warn("[400] BindException: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    /** 单参数/路径参数等约束失败（如 @Min、@NotBlank） */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> vs = e.getConstraintViolations();
        String msg = vs.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("[400] ConstraintViolationException: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    /** 缺少必需的请求参数 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseResult<Void> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = "缺少必要参数: " + e.getParameterName();
        log.warn("[400] MissingServletRequestParameterException: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    /** 类型不匹配（如 id=abc 而期待 Long） */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
    public ResponseResult<Void> handleTypeMismatch(Exception e) {
        String msg = "参数类型不匹配或转换失败: " + e.getMessage();
        log.warn("[400] TypeMismatch/ConversionFailed: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseResult<Void> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String msg = e.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(err -> {
                    String path = "";
                    String defaultMessage = err.getDefaultMessage();

                    // 安全提取字段路径
                    if (err instanceof FieldError) {
                        path = ((FieldError) err).getField();
                    } else if (err instanceof ObjectError) {
                        path = ((ObjectError) err).getObjectName();
                    } else if (err.getCodes() != null && err.getCodes().length > 0) {
                        path = err.getCodes()[0];
                    }

                    return (path == null ? "" : path) + (defaultMessage == null ? "" : ": " + defaultMessage);
                })
                .distinct()
                .collect(Collectors.joining("; "));

        if (msg.isBlank()) msg = "Validation failure";
        log.warn("[400] HandlerMethodValidationException: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    // ============================ HTTP 协议层 ============================

    /** JSON 解析失败、请求体不可读 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class})
    public ResponseResult<Void> handleHttpMessageRead(Exception e) {
        String msg = "请求体解析失败，请检查 JSON 或 Content-Type";
        log.warn("[400] HttpMessageNotReadable/Conversion: {}", e.getMessage());
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), msg);
    }

    /** 不支持的 HTTP 方法 */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseResult<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        String msg = "不允许的HTTP方法: " + e.getMethod();
        log.warn("[405] MethodNotSupported: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.BAD_METHOD.getCode(), msg);
    }

    /** 不支持的媒体类型 */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseResult<Void> handleMediaType(HttpMediaTypeNotSupportedException e) {
        String msg = "不支持的媒体类型: " + e.getContentType();
        log.warn("[415] MediaTypeNotSupported: {}", msg);
        return ResponseResult.fail(HttpStatusEnum.UNSUPPORTED_TYPE.getCode(), msg);
    }

    /** 404（需开启 throw-exception-if-no-handler-found，见文末配置） */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseResult<Void> handleNoHandler(NoHandlerFoundException e, HttpServletRequest req) {
        String msg = "资源未找到: " + req.getRequestURI();
        log.warn("[404] NoHandlerFound: {} -> {}", req.getRequestURI(), e.getMessage());
        return ResponseResult.fail(HttpStatusEnum.NOT_FOUND.getCode(), msg);
    }

    // ============================ 安全相关（Spring Security） ============================

//    /** 认证失败：用户名/密码错误 */
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
//    public ResponseResult<Void> handleAuthFail(Exception e) {
//        log.warn("[401] Auth failed: {}", e.getMessage());
//        return ResponseResult.fail(HttpStatusEnum.UNAUTHORIZED.getCode(), "认证失败：用户名或密码错误");
//    }
//
//    /** 账号状态异常（锁定/禁用等） */
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    @ExceptionHandler(AccountStatusException.class)
//    public ResponseResult<Void> handleAccountStatus(AccountStatusException e) {
//        log.warn("[403] Account status: {}", e.getMessage());
//        return ResponseResult.fail(HttpStatusEnum.FORBIDDEN.getCode(), "账号不可用或已被锁定/禁用");
//    }

    /** 鉴权失败：权限不足 */
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseResult<Void> handleAccessDenied(AccessDeniedException e) {
//        log.warn("[403] AccessDenied: {}", e.getMessage());
//        return ResponseResult.fail(HttpStatusEnum.FORBIDDEN.getCode(), "访问受限，权限不足");
//    }

    // ============================ 数据库/DAO 层（含 Oracle ORA 识别） ============================

    /** 唯一键冲突（如 username/role_code/permission.code/sn/ip/plan_code 等） */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult<Void> handleDuplicateKey(DuplicateKeyException e) {
        String detail = shortMsg(rootMessage(e));
        String msg = "唯一约束冲突（记录已存在）。可能涉及用户名/角色编码/权限编码/SN/IP/套餐编码等。详情: " + detail;
        log.warn("[409] DuplicateKey: {}", detail);
        return ResponseResult.fail(HttpStatusEnum.CONFLICT.getCode(), msg);
    }

    /** 数据完整性违规（更通用：包含外键/非空/长度/精度/唯一 等） */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseResult<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        String detail = rootMessage(e);
        String mapped = mapOracleConstraint(detail);
        log.warn("[409] DataIntegrityViolation: {} | mapped={}", detail, mapped);
        return ResponseResult.fail(HttpStatusEnum.CONFLICT.getCode(), mapped);
    }

    /** JDBC/SQL 约束异常（部分场景直接抛出此类） */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseResult<Void> handleSqlIntegrity(SQLIntegrityConstraintViolationException e) {
        String detail = rootMessage(e);
        String mapped = mapOracleConstraint(detail);
        log.warn("[409] SQLIntegrityConstraintViolation: {} | mapped={}", detail, mapped);
        return ResponseResult.fail(HttpStatusEnum.CONFLICT.getCode(), mapped);
    }

    /** SQL 语法错误（表/列名错误、拼接错误等——属服务器内部错误） */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseResult<Void> handleBadSql(BadSqlGrammarException e) {
        String detail = rootMessage(e);
        log.error("[500] BadSqlGrammar: {}", detail);
        return ResponseResult.fail(HttpStatusEnum.ERROR.getCode(), "数据库访问异常（语法/映射错误），请联系管理员");
    }

    // ============================ 上传/IO ============================

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseResult<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.warn("[400] MaxUploadSizeExceeded: {}", e.getMessage());
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), "上传文件过大");
    }

    // ============================ 业务/编程错误 & 兜底 ============================

    /** 非法参数 / 非法状态 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseResult<Void> handleIllegal(RuntimeException e) {
        log.warn("[400] Illegal arg/state: {}", e.getMessage());
        return ResponseResult.fail(HttpStatusEnum.BAD_REQUEST.getCode(), e.getMessage());
    }

    /** 运行时异常兜底（尽量靠后） */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<Void> handleRuntime(RuntimeException e, HttpServletRequest req) {
        String rid = UUID.randomUUID().toString();
        log.error("[500] RuntimeException rid={} uri={} msg={}", rid, req.getRequestURI(), e.getMessage(), e);
        return ResponseResult.fail(HttpStatusEnum.ERROR.getCode(), "系统内部错误（RID=" + rid + "）");
    }

    /** 所有异常最终兜底 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleAll(Exception e, HttpServletRequest req) {
        String rid = UUID.randomUUID().toString();
        log.error("[500] Exception rid={} uri={} msg={}", rid, req.getRequestURI(), e.getMessage(), e);
        return ResponseResult.fail(HttpStatusEnum.ERROR.getCode(), "系统内部错误（RID=" + rid + "）");
    }

    // ============================ 辅助方法 ============================

    /** 拼装字段校验错误信息 */
    private String buildFieldErrorMessage(BindingResult br) {
        List<FieldError> fieldErrors = br.getFieldErrors();
        if (fieldErrors == null || fieldErrors.isEmpty()) return "参数校验失败";
        return fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

    /** 取 Root Cause 文本 */
    private String rootMessage(Throwable e) {
        Throwable c = e;
        while (c.getCause() != null && c.getCause() != c) c = c.getCause();
        return c.getMessage() == null ? e.toString() : c.getMessage();
    }

    /** 截断过长信息，避免把堆栈全抛给前端 */
    private String shortMsg(@Nullable String msg) {
        if (msg == null) return "";
        return msg.length() > 400 ? msg.substring(0, 400) + "..." : msg;
    }

    /** 将 Oracle ORA 错误映射为更友好的提示（结合你的表结构） */
    private String mapOracleConstraint(String rootMsg) {
        String msg = shortMsg(rootMsg == null ? "" : rootMsg);
        String ora = extractOraCode(msg);

        // 常见 ORA 场景
        if ("ORA-00001".equals(ora)) {
            return "唯一约束冲突：记录已存在（可能是用户名/角色编码/权限编码/SN/IP/套餐编码等重复）";
        }
        if ("ORA-01400".equals(ora) || "ORA-01407".equals(ora)) {
            return "必填字段不能为空（请检查 NOT NULL 列）";
        }
        if ("ORA-02291".equals(ora)) {
            return "外键约束失败：父记录不存在（请检查关联ID，如 user_id/role_id/perm_id/order_id 等）";
        }
        if ("ORA-02292".equals(ora)) {
            return "外键约束失败：存在子记录依赖，无法删除/更新（如用户/角色/权限/订单存在关联）";
        }
        if ("ORA-12899".equals(ora)) {
            return "字段长度超限（请检查列定义与实际值长度，如 username/email/编码/IP/SN 等）";
        }
        if ("ORA-01438".equals(ora)) {
            return "数值超出精度范围（如金额/数量/带宽等 NUMBER(p,s) 超限）";
        }
        if ("ORA-00904".equals(ora)) {
            return "数据库列名无效（请检查字段映射/大小写/SQL 拼写）";
        }
        if ("ORA-00942".equals(ora)) {
            return "数据库对象不存在（表/视图缺失），请检查建表与权限";
        }

        // 未匹配到具体 ORA，按冲突/完整性违规的通用提示返回
        return "数据完整性约束冲突：" + msg;
    }

    /** 提取 ORA-XXXXX */
    private String extractOraCode(String s) {
        if (s == null) return "";
        Matcher m = Pattern.compile("(ORA-\\d{5})").matcher(s);
        return m.find() ? m.group(1) : "";
    }
}
