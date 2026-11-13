/* =====================================================================
   Panadería Madre Masa - Script de creación de la base de datos
   Motor: Microsoft SQL Server 2022
   Contenido: base de datos, tablas, llaves, restricciones, vistas e
              índices, más los datos mínimos de prueba.
   El script puede ejecutarse desde cero: si la base ya existe, se
   elimina y se vuelve a crear.
   ===================================================================== */

USE master;
GO

IF DB_ID(N'MadreMasa') IS NOT NULL
BEGIN
    ALTER DATABASE MadreMasa SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE MadreMasa;
END
GO

CREATE DATABASE MadreMasa;
GO

USE MadreMasa;
GO

/* ---------------------------------------------------------------------
   1. Seguridad: roles y usuarios
   --------------------------------------------------------------------- */
CREATE TABLE rol (
    id_rol        INT IDENTITY(1,1) NOT NULL,
    nombre        NVARCHAR(30)      NOT NULL,
    descripcion   NVARCHAR(150)     NULL,
    CONSTRAINT PK_rol PRIMARY KEY (id_rol),
    CONSTRAINT UQ_rol_nombre UNIQUE (nombre)
);
GO

CREATE TABLE usuario (
    id_usuario         INT IDENTITY(1,1) NOT NULL,
    codigo_empleado    NVARCHAR(10)      NOT NULL,
    nombre_completo    NVARCHAR(100)     NOT NULL,
    contrasena_hash    NVARCHAR(60)      NOT NULL,   -- BCrypt, nunca texto plano
    id_rol             INT               NOT NULL,
    activo             BIT               NOT NULL CONSTRAINT DF_usuario_activo DEFAULT (1),
    intentos_fallidos  INT               NOT NULL CONSTRAINT DF_usuario_intentos DEFAULT (0),
    bloqueado_hasta    DATETIME2(0)      NULL,
    fecha_creacion     DATETIME2(0)      NOT NULL CONSTRAINT DF_usuario_fecha DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT UQ_usuario_codigo UNIQUE (codigo_empleado),
    CONSTRAINT FK_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol),
    CONSTRAINT CK_usuario_codigo CHECK (codigo_empleado LIKE N'EMP-[0-9][0-9][0-9]'),
    CONSTRAINT CK_usuario_intentos CHECK (intentos_fallidos BETWEEN 0 AND 5)
);
GO

/* ---------------------------------------------------------------------
   2. Puntos de venta, días de operación y asignaciones
   --------------------------------------------------------------------- */
CREATE TABLE punto_venta (
    id_punto_venta  INT IDENTITY(1,1) NOT NULL,
    nombre          NVARCHAR(80)      NOT NULL,
    tipo            NVARCHAR(10)      NOT NULL,
    ubicacion       NVARCHAR(150)     NOT NULL,
    activo          BIT               NOT NULL CONSTRAINT DF_punto_activo DEFAULT (1),
    CONSTRAINT PK_punto_venta PRIMARY KEY (id_punto_venta),
    CONSTRAINT UQ_punto_venta_nombre UNIQUE (nombre),
    CONSTRAINT CK_punto_venta_tipo CHECK (tipo IN (N'CENTRO', N'FERIA'))
);
GO

-- Un punto de venta opera varios días: se separa en su propia tabla (1FN)
CREATE TABLE dia_operacion (
    id_punto_venta  INT     NOT NULL,
    dia_semana      INT     NOT NULL,   -- 1 = lunes ... 7 = domingo
    CONSTRAINT PK_dia_operacion PRIMARY KEY (id_punto_venta, dia_semana),
    CONSTRAINT FK_dia_punto FOREIGN KEY (id_punto_venta) REFERENCES punto_venta (id_punto_venta) ON DELETE CASCADE,
    CONSTRAINT CK_dia_semana CHECK (dia_semana BETWEEN 1 AND 7)
);
GO

CREATE TABLE asignacion (
    id_asignacion   INT IDENTITY(1,1) NOT NULL,
    id_usuario      INT               NOT NULL,
    id_punto_venta  INT               NOT NULL,
    fecha           DATE              NOT NULL,
    CONSTRAINT PK_asignacion PRIMARY KEY (id_asignacion),
    CONSTRAINT FK_asignacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT FK_asignacion_punto FOREIGN KEY (id_punto_venta) REFERENCES punto_venta (id_punto_venta),
    CONSTRAINT UQ_asignacion_usuario_fecha UNIQUE (id_usuario, fecha)   -- un lugar por día
);
GO

/* ---------------------------------------------------------------------
   3. Insumos e inventario
   --------------------------------------------------------------------- */
CREATE TABLE unidad_medida (
    id_unidad  INT IDENTITY(1,1) NOT NULL,
    nombre     NVARCHAR(20)      NOT NULL,
    simbolo    NVARCHAR(5)       NOT NULL,
    CONSTRAINT PK_unidad_medida PRIMARY KEY (id_unidad),
    CONSTRAINT UQ_unidad_nombre UNIQUE (nombre),
    CONSTRAINT UQ_unidad_simbolo UNIQUE (simbolo)
);
GO

CREATE TABLE insumo (
    id_insumo        INT IDENTITY(1,1) NOT NULL,
    nombre           NVARCHAR(80)      NOT NULL,
    id_unidad        INT               NOT NULL,
    existencia       DECIMAL(12,3)     NOT NULL CONSTRAINT DF_insumo_existencia DEFAULT (0),
    cantidad_minima  DECIMAL(12,3)     NOT NULL CONSTRAINT DF_insumo_minima DEFAULT (0),
    costo_unitario   DECIMAL(12,4)     NOT NULL CONSTRAINT DF_insumo_costo DEFAULT (0),
    activo           BIT               NOT NULL CONSTRAINT DF_insumo_activo DEFAULT (1),
    CONSTRAINT PK_insumo PRIMARY KEY (id_insumo),
    CONSTRAINT UQ_insumo_nombre UNIQUE (nombre),
    CONSTRAINT FK_insumo_unidad FOREIGN KEY (id_unidad) REFERENCES unidad_medida (id_unidad),
    CONSTRAINT CK_insumo_existencia CHECK (existencia >= 0),          -- RN-03
    CONSTRAINT CK_insumo_minima CHECK (cantidad_minima >= 0),
    CONSTRAINT CK_insumo_costo CHECK (costo_unitario >= 0)
);
GO

/* ---------------------------------------------------------------------
   4. Productos y recetas
   --------------------------------------------------------------------- */
CREATE TABLE producto (
    id_producto   INT IDENTITY(1,1) NOT NULL,
    nombre        NVARCHAR(80)      NOT NULL,
    precio_venta  DECIMAL(10,2)     NOT NULL,
    activo        BIT               NOT NULL CONSTRAINT DF_producto_activo DEFAULT (1),
    CONSTRAINT PK_producto PRIMARY KEY (id_producto),
    CONSTRAINT UQ_producto_nombre UNIQUE (nombre),
    CONSTRAINT CK_producto_precio CHECK (precio_venta >= 0)
);
GO

CREATE TABLE receta (
    id_receta     INT IDENTITY(1,1) NOT NULL,
    id_producto   INT               NOT NULL,
    rendimiento   INT               NOT NULL,   -- unidades por tanda
    instrucciones NVARCHAR(1000)    NULL,
    activo        BIT               NOT NULL CONSTRAINT DF_receta_activo DEFAULT (1),
    CONSTRAINT PK_receta PRIMARY KEY (id_receta),
    CONSTRAINT UQ_receta_producto UNIQUE (id_producto),   -- una receta por producto
    CONSTRAINT FK_receta_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT CK_receta_rendimiento CHECK (rendimiento > 0)          -- RN-09
);
GO

CREATE TABLE detalle_receta (
    id_receta  INT            NOT NULL,
    id_insumo  INT            NOT NULL,
    cantidad   DECIMAL(12,3)  NOT NULL,   -- en la unidad base del insumo, por tanda
    CONSTRAINT PK_detalle_receta PRIMARY KEY (id_receta, id_insumo),
    CONSTRAINT FK_detalle_receta_receta FOREIGN KEY (id_receta) REFERENCES receta (id_receta) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_receta_insumo FOREIGN KEY (id_insumo) REFERENCES insumo (id_insumo),
    CONSTRAINT CK_detalle_receta_cantidad CHECK (cantidad > 0)
);
GO

/* ---------------------------------------------------------------------
   5. Producción y movimientos de inventario
   --------------------------------------------------------------------- */
CREATE TABLE produccion (
    id_produccion        INT IDENTITY(1,1) NOT NULL,
    id_receta            INT               NOT NULL,
    tandas               INT               NOT NULL,
    unidades_producidas  INT               NOT NULL,   -- se guarda por historial: el rendimiento puede cambiar
    costo_total          DECIMAL(12,2)     NOT NULL,   -- costo al momento de producir
    id_usuario           INT               NOT NULL,
    fecha                DATETIME2(0)      NOT NULL CONSTRAINT DF_produccion_fecha DEFAULT (SYSDATETIME()),
    estado               NVARCHAR(10)      NOT NULL CONSTRAINT DF_produccion_estado DEFAULT (N'CONFIRMADA'),
    motivo_anulacion     NVARCHAR(200)     NULL,
    CONSTRAINT PK_produccion PRIMARY KEY (id_produccion),
    CONSTRAINT FK_produccion_receta FOREIGN KEY (id_receta) REFERENCES receta (id_receta),
    CONSTRAINT FK_produccion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT CK_produccion_tandas CHECK (tandas > 0),
    CONSTRAINT CK_produccion_unidades CHECK (unidades_producidas > 0),
    CONSTRAINT CK_produccion_costo CHECK (costo_total >= 0),
    CONSTRAINT CK_produccion_estado CHECK (estado IN (N'CONFIRMADA', N'ANULADA')),
    CONSTRAINT CK_produccion_anulacion CHECK (estado = N'CONFIRMADA' OR motivo_anulacion IS NOT NULL)   -- RN-12
);
GO

CREATE TABLE movimiento_inventario (
    id_movimiento         INT IDENTITY(1,1) NOT NULL,
    id_insumo             INT               NOT NULL,
    tipo                  NVARCHAR(12)      NOT NULL,
    cantidad              DECIMAL(12,3)     NOT NULL,   -- positiva suma, negativa resta
    costo_total           DECIMAL(12,2)     NULL,       -- solo en entradas
    motivo                NVARCHAR(200)     NULL,
    id_produccion         INT               NULL,
    id_movimiento_origen  INT               NULL,       -- movimiento que se anula
    id_usuario            INT               NOT NULL,
    fecha                 DATETIME2(0)      NOT NULL CONSTRAINT DF_movimiento_fecha DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_movimiento_inventario PRIMARY KEY (id_movimiento),
    CONSTRAINT FK_movimiento_insumo FOREIGN KEY (id_insumo) REFERENCES insumo (id_insumo),
    CONSTRAINT FK_movimiento_produccion FOREIGN KEY (id_produccion) REFERENCES produccion (id_produccion),
    CONSTRAINT FK_movimiento_origen FOREIGN KEY (id_movimiento_origen) REFERENCES movimiento_inventario (id_movimiento),
    CONSTRAINT FK_movimiento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT CK_movimiento_tipo CHECK (tipo IN (N'ENTRADA', N'PRODUCCION', N'AJUSTE', N'ANULACION')),
    CONSTRAINT CK_movimiento_cantidad CHECK (cantidad <> 0),
    CONSTRAINT CK_movimiento_entrada CHECK (tipo <> N'ENTRADA' OR (cantidad > 0 AND costo_total IS NOT NULL AND costo_total >= 0)),
    CONSTRAINT CK_movimiento_produccion CHECK (tipo <> N'PRODUCCION' OR (cantidad < 0 AND id_produccion IS NOT NULL)),
    CONSTRAINT CK_movimiento_ajuste CHECK (tipo <> N'AJUSTE' OR motivo IS NOT NULL),                               -- RN-11
    CONSTRAINT CK_movimiento_anulacion CHECK (tipo <> N'ANULACION' OR (id_movimiento_origen IS NOT NULL AND motivo IS NOT NULL))  -- RN-12
);
GO

/* ---------------------------------------------------------------------
   6. Cierres de venta por punto de venta
   --------------------------------------------------------------------- */
CREATE TABLE cierre_venta (
    id_cierre       INT IDENTITY(1,1) NOT NULL,
    id_punto_venta  INT               NOT NULL,
    fecha           DATE              NOT NULL,
    monto_efectivo  DECIMAL(12,2)     NOT NULL,
    monto_datafono  DECIMAL(12,2)     NOT NULL,
    total           AS (monto_efectivo + monto_datafono),   -- calculado, no se almacena
    id_usuario      INT               NOT NULL,
    fecha_registro  DATETIME2(0)      NOT NULL CONSTRAINT DF_cierre_registro DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_cierre_venta PRIMARY KEY (id_cierre),
    CONSTRAINT FK_cierre_punto FOREIGN KEY (id_punto_venta) REFERENCES punto_venta (id_punto_venta),
    CONSTRAINT FK_cierre_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT UQ_cierre_punto_fecha UNIQUE (id_punto_venta, fecha),   -- RN-15
    CONSTRAINT CK_cierre_efectivo CHECK (monto_efectivo >= 0),
    CONSTRAINT CK_cierre_datafono CHECK (monto_datafono >= 0)
);
GO

CREATE TABLE detalle_cierre (
    id_cierre          INT  NOT NULL,
    id_producto        INT  NOT NULL,
    unidades_vendidas  INT  NOT NULL,
    CONSTRAINT PK_detalle_cierre PRIMARY KEY (id_cierre, id_producto),
    CONSTRAINT FK_detalle_cierre_cierre FOREIGN KEY (id_cierre) REFERENCES cierre_venta (id_cierre) ON DELETE CASCADE,
    CONSTRAINT FK_detalle_cierre_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT CK_detalle_cierre_unidades CHECK (unidades_vendidas >= 0)
);
GO

/* ---------------------------------------------------------------------
   7. Índices para las consultas más frecuentes
   --------------------------------------------------------------------- */
CREATE INDEX IX_movimiento_insumo_fecha ON movimiento_inventario (id_insumo, fecha);
CREATE INDEX IX_produccion_fecha ON produccion (fecha);
CREATE INDEX IX_cierre_fecha ON cierre_venta (fecha);
GO

/* ---------------------------------------------------------------------
   8. Vistas: valores calculados que no se almacenan (evitan duplicidad)
   --------------------------------------------------------------------- */
-- Costo de cada receta según el costo vigente de sus insumos (RN-08)
CREATE VIEW vw_costo_receta AS
SELECT r.id_receta,
       p.nombre                                        AS producto,
       r.rendimiento,
       CAST(SUM(dr.cantidad * i.costo_unitario) AS DECIMAL(12,2))                 AS costo_tanda,
       CAST(SUM(dr.cantidad * i.costo_unitario) / r.rendimiento AS DECIMAL(12,2)) AS costo_unidad,
       p.precio_venta
FROM receta r
JOIN producto p        ON p.id_producto = r.id_producto
JOIN detalle_receta dr ON dr.id_receta = r.id_receta
JOIN insumo i          ON i.id_insumo = dr.id_insumo
GROUP BY r.id_receta, p.nombre, r.rendimiento, p.precio_venta;
GO

-- Insumos con inventario bajo (RN-10)
CREATE VIEW vw_insumo_bajo AS
SELECT i.id_insumo, i.nombre, i.existencia, i.cantidad_minima, u.simbolo
FROM insumo i
JOIN unidad_medida u ON u.id_unidad = i.id_unidad
WHERE i.activo = 1 AND i.existencia <= i.cantidad_minima;
GO

/* =====================================================================
   DATOS DE PRUEBA
   Usuarios de prueba: la contraseña de prueba se guarda solo como hash
   BCrypt. No corresponde a ninguna cuenta real.
   ===================================================================== */
INSERT INTO rol (nombre, descripcion) VALUES
 (N'ADMINISTRADOR', N'Propietaria del negocio: acceso completo.'),
 (N'PRODUCCION',    N'Empleados del centro de producción.'),
 (N'PUNTO_VENTA',   N'Empleados asignados a una feria.');

INSERT INTO usuario (codigo_empleado, nombre_completo, contrasena_hash, id_rol, activo) VALUES
 (N'EMP-001', N'Administradora de prueba', N'$2a$10$jeIARuFnUZm.WHZq.70UaO1uIkEZWhXAIGsiIBmF2Y7rYko0DXAxK', 1, 1),
 (N'EMP-002', N'Panadero de prueba',       N'$2a$10$jeIARuFnUZm.WHZq.70UaO1uIkEZWhXAIGsiIBmF2Y7rYko0DXAxK', 2, 1),
 (N'EMP-003', N'Vendedora de prueba',      N'$2a$10$jeIARuFnUZm.WHZq.70UaO1uIkEZWhXAIGsiIBmF2Y7rYko0DXAxK', 3, 1),
 (N'EMP-004', N'Empleado inactivo',        N'$2a$10$jeIARuFnUZm.WHZq.70UaO1uIkEZWhXAIGsiIBmF2Y7rYko0DXAxK', 3, 0);

INSERT INTO punto_venta (nombre, tipo, ubicacion) VALUES
 (N'Centro de producción', N'CENTRO', N'Obrador central'),
 (N'Feria A',              N'FERIA',  N'Feria del agricultor, San José'),
 (N'Feria B',              N'FERIA',  N'Feria del agricultor, Heredia');

INSERT INTO dia_operacion (id_punto_venta, dia_semana) VALUES
 (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
 (2, 6),
 (3, 5), (3, 6);

INSERT INTO asignacion (id_usuario, id_punto_venta, fecha) VALUES
 (3, 2, '2026-11-07'),
 (3, 3, '2026-11-13');

INSERT INTO unidad_medida (nombre, simbolo) VALUES
 (N'Gramo', N'g'), (N'Mililitro', N'ml'), (N'Unidad', N'u');

INSERT INTO insumo (nombre, id_unidad, cantidad_minima) VALUES
 (N'Harina de trigo',   1, 10000),
 (N'Harina integral',   1,  5000),
 (N'Agua filtrada',     2, 20000),
 (N'Sal marina',        1,  1000),
 (N'Masa madre',        1,  2000),
 (N'Semillas mixtas',   1,   500),
 (N'Mantequilla',       1,  1000);

INSERT INTO producto (nombre, precio_venta) VALUES
 (N'Pan de masa madre clásico', 2500.00),
 (N'Pan integral',              2800.00),
 (N'Pan de semillas',           3000.00);

INSERT INTO receta (id_producto, rendimiento, instrucciones) VALUES
 (1, 12, N'Autólisis de 1 h, fermentación en bloque de 4 h y horneado a 240 °C.'),
 (2, 10, N'Hidratación alta; fermentación en frío durante la noche.'),
 (3, 10, N'Hidratar las semillas antes de incorporarlas a la masa.');

INSERT INTO detalle_receta (id_receta, id_insumo, cantidad) VALUES
 (1, 1, 6000), (1, 3, 4200), (1, 4, 120), (1, 5, 1200),
 (2, 2, 4000), (2, 1, 1500), (2, 3, 4000), (2, 4, 110), (2, 5, 1100),
 (3, 1, 5000), (3, 6,  800), (3, 3, 3800), (3, 4, 100), (3, 5, 1000);

-- Entradas iniciales de inventario (compras)
INSERT INTO movimiento_inventario (id_insumo, tipo, cantidad, costo_total, id_usuario, fecha) VALUES
 (1, N'ENTRADA',  9000,  8550.00, 2, '2026-11-05 08:00'),
 (2, N'ENTRADA',  1500,  1800.00, 2, '2026-11-05 08:05'),
 (3, N'ENTRADA', 50000,   100.00, 2, '2026-11-05 08:10'),
 (4, N'ENTRADA',  5000,  3000.00, 2, '2026-11-05 08:15'),
 (5, N'ENTRADA',  6000,  3000.00, 2, '2026-11-05 08:20'),
 (6, N'ENTRADA',  2000,  8000.00, 2, '2026-11-05 08:25'),
 (7, N'ENTRADA',  2500, 16250.00, 2, '2026-11-05 08:30');

-- Costo unitario de cada insumo a partir de sus entradas (RN-07)
UPDATE i
SET costo_unitario = e.costo / e.cantidad
FROM insumo i
JOIN (SELECT id_insumo, SUM(costo_total) AS costo, SUM(cantidad) AS cantidad
      FROM movimiento_inventario WHERE tipo = N'ENTRADA' GROUP BY id_insumo) e
  ON e.id_insumo = i.id_insumo;

-- Una producción confirmada: 1 tanda de pan clásico (12 unidades)
INSERT INTO produccion (id_receta, tandas, unidades_producidas, costo_total, id_usuario, fecha)
SELECT 1, 1, 12, costo_tanda, 2, '2026-11-06 06:30' FROM vw_costo_receta WHERE id_receta = 1;

-- Salidas por la producción: cantidad de la receta por número de tandas (RN-05)
INSERT INTO movimiento_inventario (id_insumo, tipo, cantidad, id_produccion, id_usuario, fecha)
SELECT dr.id_insumo, N'PRODUCCION', -dr.cantidad * p.tandas, p.id_produccion, p.id_usuario, p.fecha
FROM produccion p
JOIN detalle_receta dr ON dr.id_receta = p.id_receta
WHERE p.id_produccion = 1;

-- Existencia de cada insumo = suma de sus movimientos
UPDATE i
SET existencia = m.total
FROM insumo i
JOIN (SELECT id_insumo, SUM(cantidad) AS total FROM movimiento_inventario GROUP BY id_insumo) m
  ON m.id_insumo = i.id_insumo;

-- Cierre de jornada de la Feria A
INSERT INTO cierre_venta (id_punto_venta, fecha, monto_efectivo, monto_datafono, id_usuario) VALUES
 (2, '2026-11-07', 45200.00, 30000.00, 3);

INSERT INTO detalle_cierre (id_cierre, id_producto, unidades_vendidas) VALUES
 (1, 1, 20), (1, 2, 9), (1, 3, 0);
GO

/* ---------------------------------------------------------------------
   Comprobación rápida
   --------------------------------------------------------------------- */
SELECT nombre, existencia, cantidad_minima, costo_unitario FROM insumo;
SELECT * FROM vw_costo_receta;
SELECT * FROM vw_insumo_bajo;
GO
