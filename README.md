# Madre Masa: sistema web de producción, inventario y costeo

Proyecto Final del curso ISB-32 Programación Avanzada (UISIL). Aplicación web para una panadería artesanal de masa
madre que produce en un centro de producción y vende en ferias del agricultor.

## Tecnologías

- Java 17, Spring Boot 3.5 (Spring MVC, Spring Data JPA, Bean Validation)
- Inicio de sesión y control de acceso por rol con HttpSession (BCrypt de spring-security-crypto para las contraseñas)
- Thymeleaf, HTML5 y CSS3
- Microsoft SQL Server 2022
- Maven (incluye Maven Wrapper)

## Módulos

| Módulo | Funciones | Roles |
|---|---|---|
| Inicio de sesión | Código de empleado y contraseña (BCrypt), bloqueo de 15 min tras 5 intentos, cierre de sesión | Todos |
| Panel | Indicadores, inventario bajo y producción reciente | Todos |
| Usuarios | Registrar, editar, restablecer contraseña, activar y desactivar empleados | Administrador |
| Puntos de venta | Registrar y editar puntos con días de operación; asignar vendedores por fecha | Administrador |
| Insumos e inventario | CRUD de insumos, entradas con costo promedio ponderado, ajustes con motivo e historial | Administrador; Producción consulta y registra entradas |
| Recetas y costeo | Registrar recetas, ingredientes, precio, costo por unidad y margen | Administrador; Producción consulta sin costos |
| Producción | Simular, ver faltantes, confirmar con descuento automático de insumos e historial | Administrador y Producción |
| Cierres | Registrar el cierre de jornada del punto asignado; consulta de cierres | Punto de venta registra; Administrador consulta |
| Ventas | Reporte por punto de venta y por producto | Administrador |

## Seguridad

- La sesión se maneja con `HttpSession`: al iniciar sesión se guardan el identificador, el código, el nombre y el
  rol del empleado. Cada método de cada controlador valida la sesión y el rol con la clase `ControlAcceso`.
- Los formularios POST llevan un token CSRF propio que se compara con el de la sesión (`ProteccionCsrf`).
- Las contraseñas se guardan con BCrypt (`spring-security-crypto`); Spring Security no se usa.
- La sesión vence a los 30 minutos de inactividad.

## Estructura

```
madremasa/
├── database/MadreMasa_BD.sql        script de creación y datos de prueba
├── src/main/java/com/madremasa/
│   ├── controller/    un controlador por módulo
│   ├── dto/           datos de formularios y reportes
│   ├── model/         entidades JPA
│   ├── repository/    repositorios de Spring Data JPA
│   ├── service/       reglas de negocio
│   └── sesion/        control de acceso con HttpSession
└── src/main/resources/
    ├── templates/     vistas Thymeleaf
    └── static/css/    hoja de estilos
```

## Cómo ejecutar

1. Ejecutar `database/MadreMasa_BD.sql` en SQL Server Management Studio. El script crea la base `MadreMasa`
   desde cero, con sus tablas, restricciones, vistas y datos de prueba.
2. Crear en SQL Server un inicio de sesión con permisos de lectura y escritura sobre `MadreMasa`.
3. Copiar `credenciales-bd.properties.ejemplo` como `credenciales-bd.properties` en la raíz del proyecto y
   completar el usuario y la contraseña. Ese archivo está en `.gitignore` y nunca se sube al repositorio.
   También se pueden usar las variables de entorno `DB_USUARIO` y `DB_CONTRASENA`.
4. Ejecutar la clase `MadreMasaApplication` desde IntelliJ IDEA (JDK 17) o con `./mvnw spring-boot:run`.
5. Abrir `http://localhost:8080`.

## Usuarios de prueba

| Código  | Rol            | Estado   |
|---------|----------------|----------|
| EMP-001 | Administrador  | Activo   |
| EMP-002 | Producción     | Activo   |
| EMP-003 | Punto de venta | Activo   |
| EMP-004 | Punto de venta | Inactivo |

Los usuarios de prueba comparten una contraseña de prueba que se entrega por separado a la persona docente. En la
base de datos solo se guarda su hash BCrypt.

## Problemas conocidos

| Problema | Solución |
|---|---|
| `Login failed for user` al iniciar la aplicación | Revisar el usuario y la contraseña de `credenciales-bd.properties` o de las variables de entorno. |
| `Connection refused` en el puerto 1433 | Habilitar TCP/IP en SQL Server Configuration Manager o cambiar `DB_PUERTO`. |
| Un formulario vuelve al inicio de sesión con el aviso "La página había expirado" | El token CSRF venció porque la aplicación se reinició; volver a ingresar. |
| "Cuenta bloqueada temporalmente" | Esperar 15 minutos o que el administrador restablezca la contraseña del empleado. |
