# Distribuidora — App Android de pedidos (agua, hielo y cualquier producto)

App nativa Android (Kotlin + Jetpack Compose) para administrar **clientes, productos y pedidos**, con
**filtro de pendientes**, **fecha de entrega** por pedido y **trazado de ruta en Google Maps** usando
las coordenadas de cada cliente. Los datos se guardan **localmente en el teléfono** (base de datos Room),
así que funciona sin internet y sin costos de servidor.

> Aunque está pensada para agua y hielo, sirve para cualquier rubro: los "productos" son genéricos
> (nombre, precio, unidad), así que podés cargar lo que vendas.

---

## 1. Cómo obtener el archivo APK (la forma más fácil, sin instalar nada)

Como el APK se compila con herramientas de Android, lo más simple es dejar que **GitHub** lo compile
por vos en la nube. Es gratis. Ya dejé todo configurado.

1. Creá una cuenta gratis en https://github.com (si no tenés).
2. Creá un repositorio nuevo (botón **New**), por ejemplo `distribuidora`. Dejalo público o privado.
3. Subí **todo el contenido de esta carpeta** al repositorio:
   - Opción fácil: en la página del repo vacío, botón **"uploading an existing file"**, y arrastrás
     todos los archivos y carpetas de aquí (incluida la carpeta `app` y `.github`).
   - O con Git: `git init`, `git add .`, `git commit -m "primer commit"`, `git branch -M main`,
     `git remote add origin <URL-de-tu-repo>`, `git push -u origin main`.
4. Apenas subís el código, se ejecuta solo el proceso de compilación. Entrá a la pestaña **Actions**
   del repo y esperá a que el trabajo **"Compilar APK"** termine (tilde verde, ~3-5 min).
5. Abrí ese trabajo terminado y, abajo en **Artifacts**, descargá **`app-debug-apk`**.
   Adentro está el archivo **`app-debug.apk`**.

> Si no se ejecutó solo: pestaña **Actions** → **Compilar APK** → botón **Run workflow**.

---

## 2. Cómo instalar el APK en tu celular

1. Pasá el archivo `app-debug.apk` al teléfono (por cable, Google Drive, WhatsApp a vos mismo, etc.).
2. Abrílo desde el celular. Android va a pedir permitir **"instalar apps de orígenes desconocidos"**:
   aceptá para la app desde la que lo abrís (Archivos / Chrome).
3. Tocá **Instalar**. Listo: aparece la app **"Distribuidora"** en tu menú.

Es un APK de depuración (firmado con la clave de prueba de Android), perfecto para uso personal.
Para publicarlo en Google Play más adelante hace falta una versión *release* firmada (ver sección 5).

---

## 3. Cómo usar la app

- **Pedidos** (pantalla inicial): lista de pedidos. Arriba podés filtrar **Pendientes** o **Todos**.
  - Botón **+**: crear un pedido nuevo (elegís cliente, agregás productos con cantidad, ponés fecha de entrega).
  - En cada pedido: ✓ para marcar **entregado**, 📍 para abrir la ubicación del cliente, 🗑 para borrar.
- **Ruta**: toma los pedidos **pendientes que tienen ubicación**, los ordena por cercanía
  (vecino más cercano) y abre **Google Maps** con todas las paradas. Podés tildar cuáles incluir y
  partir desde tu ubicación actual.
- **Clientes**: alta/edición de clientes con teléfono, dirección, notas y **coordenadas**
  (botón "Usar mi ubicación actual" o cargadas a mano).
- **Productos**: alta/edición de productos con precio y unidad (bolsa, bidón, etc.).

**Sugerencia de uso:** primero cargá algunos **productos**, luego **clientes** (con su ubicación),
y después ya podés crear **pedidos** y trazar la **ruta**.

---

## 4. Abrir y modificar el proyecto a futuro (Android Studio)

El código fuente completo está en esta carpeta para que lo edites cuando quieras.

1. Instalá **Android Studio** (gratis): https://developer.android.com/studio
2. **File → Open** y seleccioná esta carpeta. Android Studio descarga solo lo que falta
   (Gradle y dependencias) en el primer arranque.
3. Para generar el APK desde Android Studio: menú **Build → Build Bundle(s)/APK(s) → Build APK(s)**.
   El APK queda en `app/build/outputs/apk/debug/`.

Estructura principal del código (`app/src/main/java/com/distribuidora/app/`):

- `data/` — base de datos (Room): entidades `Cliente`, `Producto`, `Pedido`, `PedidoItem`, DAOs y repositorio.
- `ui/` — pantallas Compose, ViewModels, tema y utilidades de formato.
- `util/` — armado de la URL de Google Maps y obtención de ubicación.
- `MainActivity.kt` — navegación con la barra inferior.

---

## 5. Notas técnicas

- **Tecnología:** Kotlin, Jetpack Compose (Material 3), Room, Navigation Compose, Play Services Location.
- **Mínimo de Android:** 8.0 (API 26) en adelante.
- **Datos locales:** todo queda en el teléfono. Si desinstalás la app, se borran los datos.
- **Ruta:** Google Maps admite hasta ~9 paradas por viaje; si hay más pedidos, hacé la ruta en tandas.
- **Versión para Google Play (release firmada):** generá un *keystore* y configurá `signingConfigs`
  en `app/build.gradle.kts`; luego `Build → Generate Signed Bundle / APK`.

---

¿Querés que sume funciones como exportar pedidos a Excel/PDF, sincronización en la nube entre varios
repartidores, o un mapa integrado dentro de la app? Se puede agregar sobre esta misma base.
