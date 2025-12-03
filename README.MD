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
- **Parsers JSON**: Transforman los datos obtenidos desde la API MITMA en entidades Java.
- **Gestión concurrente**: Procesamiento en paralelo de grandes volúmenes de datos para reducir el tiempo de
  actualización.
- **Persistencia**: Sincronización eficiente con la base de datos Supabase usando operaciones por lotes.

---

## 🔧 Configuración inicial

Antes de ejecutar el cron, **es imprescindible configurar correctamente las variables de entorno** relacionadas con la
conexión a Supabase.

### Variables necesarias:

| Variable  | Descripción                                 |
|-----------|---------------------------------------------|
| `DB_URL`  | Dirección del host de la base de datos      |
| `DB_USER` | Usuario con permisos de lectura y escritura |
| `DB_PASS` | Contraseña correspondiente                  |


> ⚠️ **Importante:**  
> Si estas variables no se definen correctamente, el cron **no podrá establecer conexión con la base de datos** y **no
se iniciará el proceso de actualización**.

---

## 🔄 Funcionamiento general

### ⏱️ Ejecución programada

El cron se activa automáticamente **cada 30 minutos** mediante un planificador interno basado en
`ScheduledExecutorService`. La programación es configurable desde las properties del proyecto.

### 🌐 Obtención de datos

Realiza **peticiones HTTP** a la **API de Hidrocarburos del MITMA**, obteniendo un JSON con la información de todas las
estaciones de servicio y precios actualizados.

### 🧩 Procesamiento y parseo

Los datos recibidos se transforman en entidades (`EESS`, `Municipio`, `Provincia`, etc.) mediante parsers dedicados.

### 💾 Persistencia eficiente

Las entidades se sincronizan con la base de datos Supabase usando **operaciones por lotes (batch)** y **procesamiento
concurrente con hilos** para optimizar el rendimiento.

### 📋 Registro de actividad

Cada ejecución genera logs detallados con:

- Número de estaciones procesadas.
- Errores detectados.
- Tiempo total de ejecución.

---

## ⚠️ Principales desafíos técnicos

### 🧵 Gestión de hilos y concurrencia

Uno de los mayores retos fue la **gestión eficiente del gran volumen de datos** (más de 10.000 estaciones por
actualización).  
Procesar y almacenar esta cantidad en un tiempo razonable requería:

- Uso de `ExecutorService` y `CompletableFuture` para ejecución multihilo.
- Control estricto del número de hilos concurrentes.
- Sincronización de colecciones compartidas.
- Cierre correcto de `EntityManager` y conexiones al finalizar cada tarea.

Sin estas medidas, aparecían **fugas de memoria**, **bloqueos** y **errores de conexión**.  
El código actual implementa una solución robusta que equilibra concurrencia, rendimiento y estabilidad.

---

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

*En el sistema final la BD será local, en un servior porpio por lo que no habrá este problema.

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
- ⚙️ Persistencia concurrente estable y validada.
- 🔄 Sistema de cron en producción cada 30 minutos.
- 🧩 Pendiente de optimizaciones menores y mejora del sistema de logs.

---

## 👨‍💻 Autor

**Manuel García Baldo**  
Universidad de Oviedo  
Trabajo de Fin de Grado - Ingeniería Informática (2025)

---

## 🧾 Licencia

Este proyecto se distribuye bajo licencia **MIT**.  
Puede utilizarse, modificarse o redistribuirse libremente, siempre que se cite la fuente original.
