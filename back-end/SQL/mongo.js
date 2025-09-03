// 1) 订单读模型
db.createCollection("order_views", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["_id","customerName","status","createdAt"],
            properties: {
                _id: { bsonType: "long", description: "orderId" },
                customerName: { bsonType: "string" },
                status: { enum: ["CREATED","RESERVED","ASSIGNED","ON_SITE","DONE","BILLED","CANCELED","TERMINATED"] },
                createdAt: { bsonType: "date" },
                extras: { bsonType: "object" }
            }
        }
    }
});
db.order_views.createIndex({ status: 1, createdAt: -1 });

// 2) 流程时间线
db.createCollection("workflow_timeline", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["orderId","step","status","ts"],
            properties: {
                orderId: { bsonType: "long" },
                step: { bsonType: "string" },
                status: { enum: ["PENDING","SUCCEEDED","FAILED","COMPENSATED"] },
                in: { bsonType: ["object","string"] },
                out:{ bsonType: ["object","string"] },
                ts:  { bsonType: "date" }
            }
        }
    }
});
db.workflow_timeline.createIndex({ orderId: 1, ts: -1 });

// 3) 审计日志
db.createCollection("operation_logs", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["who","action","targetType","targetId","ts"],
            properties: {
                who: { bsonType: "string" },
                action: { bsonType: "string" },
                targetType: { bsonType: "string" },
                targetId: { bsonType: ["long","string"] },
                ip: { bsonType: "string" },
                ts: { bsonType: "date" }
            }
        }
    }
});
db.operation_logs.createIndex({ targetType: 1, targetId: 1, ts: -1 });

// 4) 通知
db.createCollection("notifications", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["userId","title","content","read","ts"],
            properties: {
                userId: { bsonType: "long" },
                title: { bsonType: "string" },
                content: { bsonType: "string" },
                read: { bsonType: "bool" },
                ts: { bsonType: "date" }
            }
        }
    }
});
db.notifications.createIndex({ userId: 1, read: 1, ts: -1 });

// 5) 报表快照
db.createCollection("report_snapshots", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["date","generatedAt"],
            properties: {
                date: { bsonType: "string", description: "YYYY-MM-DD" },
                kpis: { bsonType: "object" },
                generatedAt: { bsonType: "date" }
            }
        }
    }
});
db.report_snapshots.createIndex({ date: 1 }, { unique: true });

// 6) 动态表单配置
db.createCollection("config_forms", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["key","schemaJson","updatedAt"],
            properties: {
                key: { bsonType: "string" },
                schemaJson: { bsonType: "object" },
                updatedAt: { bsonType: "date" }
            }
        }
    }
});
db.config_forms.createIndex({ key: 1 }, { unique: true });
