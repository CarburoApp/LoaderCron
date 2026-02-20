# 🕒 Carburo - Cron de Actualización Automática de Datos de Hidrocarburos (TFG)

## 📊 Calidad del Código (SonarCloud)

**Estado General**

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)

**Bugs y Vulnerabilidades**

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=bugs)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)

**Cobertura y Mantenibilidad**

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=coverage)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=CarburoApp_LoaderCron&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=CarburoApp_LoaderCron)

---

## 📘 Introducción

Este repositorio forma parte del **Trabajo de Fin de Grado (TFG)** de **Manuel García Baldo** en la **Universidad de
Oviedo**.  
Su objetivo es **mantener actualizados los datos de estaciones de servicio y precios de combustibles en España**,
obtenidos desde la **API de Hidrocarburos del MITMA**, para que estén disponibles y actualizados en la **aplicación
Android principal del proyecto**.

Este módulo se ejecuta como un **cron programado cada 30 minutos**, que descarga, procesa y sincroniza la información
con una **base de datos Supabase** (PostgreSQL).  
De esta forma, la aplicación Android puede consultar los datos más recientes sin depender directamente de la API
externa, mejorando la **disponibilidad, velocidad y fiabilidad**.

---

## ⚙️ Estructura general del proyecto

El proyecto incluye:

- **Sistema de cron**: Planificador automático que ejecuta las actualizaciones cada 30 minutos como un job.
- **Módulo HTTP**: Gestiona las peticiones y la recepción de datos JSON.
- **Parsers JSON -> DTO -> Entidades**: Transforman los datos obtenidos desde la API MITMA en entidades Java.
- **Persistencia**: Sincronización eficiente con la base de datos Supabase usando operaciones por lotes.
- **Servicio de Loggers**: Toda la ejecución queda registrada en los logs correspondientes.
- **Servicio de correo**: Dispone de un servicio de correo para enviar los resultados de la ejeución e informar de
  cualquier posible error durante su ejecución.

---

## 🔧 Configuración inicial

Antes de ejecutar el cron, es **imprescindible configurar correctamente las variables de entorno** relacionadas con la
conexión a la base de datos y, de manera opcional, el servicio de correo.

### 1️⃣ Variables necesarias para la base de datos

| Variable  | Descripción                                 |
|-----------|---------------------------------------------|
| `DB_URL`  | Dirección del host de la base de datos      |
| `DB_USER` | Usuario con permisos de lectura y escritura |
| `DB_PASS` | Contraseña correspondiente                  |


> ⚠️ **Importante:**  
> Si estas variables no se definen correctamente, los servicios **no podrán establecer conexión con la base de datos** y
**no se iniciará el proceso de actualización**.

---

### 2️⃣ Variables de entorno para el correo (opcional)

Para habilitar el envío de notificaciones por correo (alertas de errores o avisos importantes), se deben definir estas
variables de entorno:

| Variable            | Descripción                                                      |
|---------------------|------------------------------------------------------------------|
| `ENV_MAIL_EMAIL`    | Dirección de correo desde la que se enviarán los mensajes        |
| `ENV_MAIL_PASSWORD` | Contraseña (mucho mejor Token de acceso) de la cuenta de correo  |
| `ENV_MAIL_TO_EMAIL` | Dirección o lista de correos destinatarios de las notificaciones |

> ⚠️ **Recomendación de seguridad:**
> - Nunca incluir la contraseña (`ENV_MAIL_PASSWORD`) directamente en el repositorio.
> - Defínela como variable de entorno en el servidor:

### 3️⃣ Configuración de certificados SSL (opcional)

En algunos entornos, las llamadas HTTPS a la API del MITMA pueden fallar debido a que Java no confía en el certificado del servidor. 
Para evitar errores como `Connection reset` o problemas de handshake SSL, puedes configurar un certificado **Root CA** confiable manualmente.

---

#### 📌 ¿Qué hay que hacer?

1. Exportar el certificado Root CA del MITMA (por ejemplo `mitma-root.cer`).
    - Este certificado se puede obtener desde el navegador o con `openssl`/PowerShell.
    - Debe ser la **CA raíz**, no el certificado del endpoint, para que no caduque cada año.

2. Colocar el archivo **mitma-root.cer** en la raíz del proyecto.

---

## 🔄 Funcionamiento general

### ⏱️ Ejecución programada

El cron se activa automáticamente **cada 30 minutos** mediante un planificador interno basado en
`ScheduledExecutorService` mediante la libería de Quartz. La programación es configurable desde las properties del
proyecto.

### 🌐 Obtención de datos

Realiza **peticiones HTTP** a la **API de Hidrocarburos del MITMA**, obteniendo un JSON con la información de todas las
estaciones de servicio y precios actualizados.

### 🧩 Procesamiento y parseo

Los datos recibidos se transforman en entidades (`EESS`, `Municipio`, `Provincia`, etc.) mediante parsers dedicados.

### 💾 Persistencia eficiente

Las entidades se sincronizan con la base de datos Supabase usando **operaciones por lotes (batch)** o **procesamiento
concurrente con hilos** para optimizar el rendimiento.

### 📋 Registro de actividad

Cada ejecución genera logs detallados con:

- Número de estaciones procesadas.
- Errores detectados.
- Tiempo total de ejecución.
- etc

---

## ⚠️ Principales desafíos técnicos

### 🌐 Problema con IPv4 e IPv6 en Supabase

Durante el desarrollo se detectó un problema crítico con la **resolución de direcciones IP**:

- El entorno local del cron usaba **IPv4**.
- Supabase, en su infraestructura actual, **solo acepta tráfico entrante por IPv6** en su versión Online, con la que se
  empezó a trabajar.

Esto generaba errores de conexión del tipo `Connection refused` o `timeout`, incluso con credenciales correctas.  
La causa estaba en la resolución DNS: el dominio de Supabase devolvía registros **AAAA (IPv6)**, mientras el entorno
local intentaba conectarse por IPv4.

#### ✅ Solución adoptada:

- Ejecutar el cron en entornos con **soporte IPv4 nativo**. Con la BD Supabase en un modo especial para poder aceptar
  conexiones IPV4.
- En entornos sin IPv6, usar **contenedores o máquinas dual-stack (IPv4/IPv6)** para permitir la conexión.

*En el sistema final la BD será local, en un servior propio por lo que no habrá este problema.

---

## 📱 Relación con la aplicación Android del TFG

El cron actúa como **motor de actualización** de los datos que la **aplicación Android del TFG** utiliza.  
La app permite a los usuarios:

- Consultar **precios actualizados** de combustibles (Gasolina 95, Gasóleo A, GLP, etc.).
- **Localizar estaciones** cercanas con mapas interactivos.
- Ver **horarios, direcciones y coordenadas**.
- Aplicar **filtros por tipo de combustible o ubicación**.

El cron garantiza que estos datos estén siempre actualizados en Supabase, sin depender directamente de la API MITMA (más
lenta y menos estable para uso en producción).

---

## 🚀 Estado actual

- ✅ Código operativo y funcional.
- ⚙️ Persistencia estable y validada.
- 🔄 Sistema de cron en producción cada 30 minutos.
- 🧩 Pendiente de optimizaciones menores y mejora del sistema de logs.

---

## 🛠️ Sistema de despliegue

El despliegue de los servicios *cron* y *backend* en el servidor se realiza mediante un **sistema de auto-actualización
automática**.  
Este sistema se encarga de:

- Comprobar periódicamente si hay nuevas releases publicadas en GitHub.
- Descargar y reemplazar los JAR correspondientes.
- Reiniciar los servicios de forma segura.
- Garantizar arranque automático tras reinicios del servidor.

Este enfoque permite mantener los servicios **actualizados y seguros** sin exponer puertos ni la IP del servidor,
evitando la necesidad de CI/CD remoto que conecte directamente con la infraestructura.

> Para ver todos los detalles de configuración, scripts, servicios systemd y timers, consulta el repositorio completo:  
> [Server-Cron-Backend-AutoUpdate](https://github.com/CarburoApp/Server-Cron-Backend-AutoUpdate.git)

---

## ⚙️ Configuración del Cron

El cron dispone de **archivos de propiedades** que permiten ajustar su comportamiento y adaptarlo a diferentes entornos
o bases de datos.

### Archivos principales

1. **`application.properties`**  
   Contiene la configuración general del cron, incluyendo conexión a la base de datos, parámetros del scheduler, logging
   y correo.

2. **`queries.properties`**  
   Contiene las consultas SQL que se ejecutan para sincronizar los datos con la base de datos.
   > ⚠️ Si cambias la estructura de la BD o deseas personalizar las consultas, aquí es donde se deben adaptar.

3. **Endpoints de la API**  
   Los endpoints utilizados para obtener los datos desde la API del MITMA están definidos dentro del proyecto y pueden
   modificarse si fuese necesario cambiar la fuente de datos.

### Uso de la carpeta `/config`

- Si existe la carpeta `/config` dentro del directorio donde se encuentra el cron, **el servicio cargará los archivos de ahí**.
- Si no existe, el cron **usará los valores por defecto** incluidos en el JAR, debiendo, aún así, introducir las variables de entorno.
- Esto permite:
  - Mantener configuraciones personalizadas sin modificar el JAR original.
  - Adaptar queries, endpoints o propiedades generales de forma sencilla según el entorno.

> 🔹 **Recomendación:**  
> Puedes crear tus propias versiones de `application.properties`, `queries.properties` y `endpoints.properties` dentro de `/config` para ajustar el cron a entornos específicos, bases de datos distintas o cambios en la API.

---

## 👨‍💻 Autor

**Manuel García Baldo**  
Universidad de Oviedo  
Trabajo de Fin de Grado - Ingeniería Informática (2025)

---

## 🧾 Licencia

Este proyecto se distribuye bajo licencia **MIT**.  
Puede utilizarse, modificarse o redistribuirse libremente, siempre que se cite la fuente original.
