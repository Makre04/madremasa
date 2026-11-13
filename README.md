# Madre Masa: sistema web de producción, inventario y costeo

Proyecto Final del curso ISB-32 Programación Avanzada (UISIL). Aplicación web para una panadería artesanal de masa
madre que produce en un centro de producción y vende en ferias del agricultor.

## Tecnologías

- Java 17, Spring Boot 3.5 (Spring MVC, Spring Data JPA, Spring Security, Bean Validation)
- Thymeleaf, HTML5 y CSS3
- Microsoft SQL Server 2022
- Maven (incluye Maven Wrapper)

## Estructura

```
madremasa/
├── database/MadreMasa_BD.sql        script de creación y datos de prueba
├── src/main/java/com/madremasa/
│   ├── config/        seguridad
│   ├── controller/    un controlador por módulo
│   ├── dto/           datos de formularios y reportes
│   ├── model/         entidades JPA
│   ├── repository/    repositorios de Spring Data JPA
│   └── service/       reglas de negocio
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
