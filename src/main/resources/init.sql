-- 客户订购管理系统 v2.0 数据库初始化

CREATE TABLE IF NOT EXISTS customers (
    customer_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    contact       TEXT,
    phone         TEXT,
    email         TEXT,
    address       TEXT,
    created_at    TEXT    DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS goods (
    goods_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_name    TEXT    NOT NULL,
    category      TEXT    DEFAULT '其他',
    unit          TEXT    NOT NULL DEFAULT '件',
    unit_price    REAL    NOT NULL DEFAULT 0.0,
    stock         INTEGER NOT NULL DEFAULT 0,
    description   TEXT,
    created_at    TEXT    DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS orders (
    order_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id   INTEGER NOT NULL,
    order_date    TEXT    DEFAULT (datetime('now','localtime')),
    total_amount  REAL    DEFAULT 0.0,
    status        TEXT    DEFAULT '待处理',
    remark        TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS order_items (
    item_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id      INTEGER NOT NULL,
    goods_id      INTEGER NOT NULL,
    quantity      INTEGER NOT NULL DEFAULT 1,
    unit_price    REAL    NOT NULL,
    subtotal      REAL    NOT NULL,
    FOREIGN KEY (order_id)  REFERENCES orders(order_id),
    FOREIGN KEY (goods_id)  REFERENCES goods(goods_id)
);

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id      INTEGER NOT NULL UNIQUE,
    invoice_no    TEXT    NOT NULL UNIQUE,
    issue_date    TEXT    DEFAULT (datetime('now','localtime')),
    amount        REAL    NOT NULL,
    tax_rate      REAL    DEFAULT 0.13,
    tax_amount    REAL,
    total_amount  REAL,
    status        TEXT    DEFAULT '未付款',
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 示例数据由 DatabaseManager.java 在表为空时插入
