# 🚀 To-Do List API - Spring Boot & CI/CD Pipeline

API RESTful desarrollada en Java con **Spring Boot** que implementa un CRUD completo para la gestión de "Tareas por Hacer" (To-Do List), acompañada de un pipeline automatizado de **CI/CD con 5 etapas** para compilar, probar, verificar calidad, empaquetar y desplegar automáticamente en una instancia **EC2 en AWS** vía SSH.

---

## 🛠️ Tecnologías Utilizadas

- **Java 21 LTS**
- **Spring Boot 4 / Spring Framework**
  - Spring Web MVC
  - Spring Data JPA
  - H2 Database (base de datos en memoria y consola `/h2-console`)
  - Bean Validation (`jakarta.validation`)
  - Lombok
- **Maven Wrapper (`./mvnw`)**
- **JUnit 5 & Mockito** (Pruebas unitarias completas)
- **GitLab CI/CD & GitHub Actions**
- **AWS EC2 (Ubuntu)**

---

## 📋 Endpoints de la API REST

Base URL: `http://3.145.138.105:8080` (en EC2) o `http://localhost:8080` (local)

| Método | Endpoint | Descripción | Body / Parámetros |
|---|---|---|---|
| `GET` | `/` | Información general de la API | - |
| `GET` | `/api/tasks/health` | Health check del servicio (UP) | - |
| `GET` | `/api/tasks` | Listar todas las tareas | Opcional: `?completed=true|false` |
| `GET` | `/api/tasks/{id}` | Obtener una tarea por su ID | - |
| `POST` | `/api/tasks` | Crear una nueva tarea | JSON con `title` y opcional `description` |
| `PUT` | `/api/tasks/{id}` | Actualizar una tarea existente | JSON con datos actualizados |
| `PATCH`| `/api/tasks/{id}/toggle` | Alternar estado completado/pendiente | - |
| `DELETE`| `/api/tasks/{id}` | Eliminar una tarea | - |

### Ejemplos de uso con `curl`:

#### 1. Health Check
```bash
curl -X GET http://localhost:8080/api/tasks/health
```

#### 2. Crear una Tarea (POST)
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Configurar pipeline CI/CD",
    "description": "Implementar las 5 etapas en GitLab CI y desplegar en EC2"
  }'
```

#### 3. Listar Tareas (GET)
```bash
curl -X GET http://localhost:8080/api/tasks
```

#### 4. Obtener por ID (GET)
```bash
curl -X GET http://localhost:8080/api/tasks/1
```

#### 5. Actualizar Tarea (PUT)
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Configurar pipeline CI/CD - Completado",
    "description": "5 etapas configuradas y probadas con éxito",
    "completed": true
  }'
```

#### 6. Alternar estado (PATCH)
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/toggle
```

#### 7. Eliminar Tarea (DELETE)
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

---

## 🔄 Estructura del Pipeline CI/CD (5 Etapas)

El archivo `.gitlab-ci.yml` define un flujo automatizado de 5 fases:

```
[ 1. build ] ➔ [ 2. test ] ➔ [ 3. quality ] ➔ [ 4. packing ] ➔ [ 5. deploy ]
```

### 1. `build` (Compilación)
- **Propósito**: Descargar dependencias y verificar la compilación del código fuente.
- **Comando**: `./mvnw compile`
- **Artefactos**: Clases compiladas en `target/classes/`.

### 2. `test` (Pruebas Unitarias)
- **Propósito**: Ejecutar todas las pruebas unitarias y de integración de servicios y controladores con JUnit 5 y Mockito.
- **Comando**: `./mvnw test`
- **Reporte**: Genera reportes JUnit XML en `target/surefire-reports/`. Si algún test falla, el pipeline se detiene protegiendo producción.

### 3. `quality` (Calidad y Empaquetado de Artefacto)
- **Propósito**: Ejecutar empaquetado del JAR ejecutable (`./mvnw package -DskipTests`) y preparar la estructura de directorios `build/libs/to-do-list-api-0.0.1-SNAPSHOT.jar` requerida para el despliegue.
- **Artefactos**: El archivo `.jar` ejecutable generado y guardado para las siguientes fases.

### 4. `packing` (Transferencia Segura del Artefacto a AWS EC2)
- **Propósito**: Instalar cliente SSH (`apk add openssh-client`), ajustar permisos de la llave privada (`chmod 600 $AWS_KEY`), crear directorio de destino en la instancia EC2 y transferir el JAR vía `scp` a `/home/ubuntu/to-do-list-api/api.jar`.
- **Comando clave**:
  ```bash
  scp -o StrictHostKeyChecking=no -i $AWS_KEY build/libs/to-do-list-api-0.0.1-SNAPSHOT.jar ubuntu@3.145.138.105:/home/ubuntu/to-do-list-api/api.jar
  ```

### 5. `deploy` (Despliegue y Puesta en Producción)
- **Propósito**: Conectar a EC2 por SSH, terminar cualquier proceso previo de la aplicación (`nohup pkill -f 'java -jar'`), iniciar la nueva versión en segundo plano (`nohup java -jar ... &`), y verificar que el proceso esté activo.
- **Comando clave**:
  ```bash
  nohup ssh -o StrictHostKeyChecking=no -i $AWS_KEY ubuntu@3.145.138.105 "nohup java -jar /home/ubuntu/to-do-list-api/api.jar > /home/ubuntu/to-do-list-api/app.log 2>&1 &"
  ```
- **Ambiente**: Marcado como `environment: production`.

---

## 🔑 Configuración en GitLab

Para que el pipeline pueda conectarse a la instancia de AWS EC2:

1. Ve a tu proyecto en **GitLab** > **Settings** > **CI/CD** > **Variables**.
2. Agrega la variable:
   - **Key**: `AWS_KEY`
   - **Type**: `File` (o `Variable`)
   - **Value**: El contenido completo de tu llave privada `.pem` de AWS (incluyendo `-----BEGIN RSA PRIVATE KEY-----` y `-----END RSA PRIVATE KEY-----`).
   - Marca las opciones *Protect variable* (si usas ramas protegidas) y *Mask variable* si corresponde.
3. Al hacer `git push`, GitLab ejecutará automáticamente las 5 etapas.

---

## 💻 Ejecución y Pruebas Locales

```bash
# Compilar
./mvnw compile

# Ejecutar pruebas unitarias
./mvnw test

# Empaquetar JAR
./mvnw package -DskipTests

# Ejecutar la aplicación localmente
./mvnw spring-boot:run
# O directamente el JAR:
java -jar target/to-do-list-api-0.0.1-SNAPSHOT.jar
```
Consola H2 local disponible en: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:todolistdb`, usuario: `sa`, sin contraseña).
