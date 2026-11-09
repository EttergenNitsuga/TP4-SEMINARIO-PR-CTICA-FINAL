TP4 - SISTEMA DE GESTION DE RESERVAS: CABANAS EL HORNERO
========================================================

CONTENIDO
- src/ar/edu/hornero/: los 19 archivos Java del proyecto.
- sql/01_crear_tablas.sql: crea la base y las tablas (esquema de la Figura 1 del informe).
- sql/02_datos_prueba.sql: datos de demostracion (usuarios, clientes, cabanas, reservas, pagos).
- sql/03_reiniciar_demo.sql: vuelve la base de demostracion al estado inicial.
- compilar.bat: compila el proyecto en Windows.
- probar_modelo.bat: ejecuta pruebas sin necesitar MySQL.
- ejecutar_menu.bat: ejecuta el menu conectado a MySQL.
- lib/: aqui va el JAR de MySQL Connector/J.
- docs/: aqui va el informe PDF del TP4.

REQUISITOS
1. JDK 17 o superior (recomendado JDK 21).
2. MySQL Server 8.0.16 o superior y MySQL Workbench.
3. MySQL Connector/J: un archivo .jar dentro de la carpeta lib.

PASO A PASO EN WINDOWS

A. Descomprimir
1. Descomprime este ZIP en una carpeta sencilla, por ejemplo C:\TP4_Cabanas_El_Hornero.
2. Abre esa carpeta en el Explorador de archivos.

B. Preparar MySQL
1. Abre MySQL Workbench y conecta con tu usuario administrador (por ejemplo root).
2. Si ya existe una base cabanas_el_hornero con datos que quieras conservar, NO ejecutes
   estos scripts encima: haz antes una copia de seguridad (Server > Data Export).
3. Abre sql/01_crear_tablas.sql y ejecuta todo el script con el icono del rayo.
4. Abre sql/02_datos_prueba.sql y ejecuta todo el script (base nueva y vacia).
5. Crea el usuario limitado de la aplicacion, sustituyendo la clave de ejemplo por una propia:

   CREATE USER IF NOT EXISTS 'hornero_app'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE';
   ALTER USER 'hornero_app'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE';
   GRANT SELECT, INSERT, UPDATE ON cabanas_el_hornero.* TO 'hornero_app'@'localhost';

   Ejecuta esas sentencias como administrador. El programa usa id_usuario=2 (recepcionista),
   que carga el script de datos de prueba.

C. Instalar el conector JDBC
1. Descarga MySQL Connector/J desde la pagina oficial de MySQL:
   https://dev.mysql.com/downloads/connector/j/
2. Descomprime el paquete si viene comprimido.
3. Copia el archivo mysql-connector-j-<version>.jar dentro de la carpeta lib de este proyecto.
   No cambies el codigo: el archivo .bat toma todos los JAR que haya en lib.

D. Configurar credenciales y ejecutar el menu
1. Abre CMD dentro de la carpeta raiz del proyecto (donde estan compilar.bat y ejecutar_menu.bat).
   Consejo: en el Explorador escribe cmd en la barra de direcciones y presiona Enter.
2. Escribe estas variables, usando la clave que elegiste en MySQL:

   set HORNERO_DB_HOST=localhost
   set HORNERO_DB_USER=hornero_app
   set HORNERO_DB_PASS=TU_CLAVE

3. Ejecuta:

   ejecutar_menu.bat

El programa compila e inicia el menu. Si cambias las credenciales, repite los comandos set
en esa misma ventana CMD.

E. Ejecutar primero la prueba sin MySQL (recomendado antes de la demostracion)
1. Desde la carpeta raiz, haz doble clic en probar_modelo.bat.
2. Se compila y se ejecuta PruebaModelo. Esta prueba NO necesita MySQL ni Connector/J.
3. Debes ver: cabanas ordenadas por tarifa, calculo de 3 noches por $210000, validacion de
   fechas, reporte anual de 9 noches, archivos CSV/TXT/LOG y deteccion SQLSTATE 23000.

DATOS DE PRUEBA Y TABLA 9 DEL INFORME
Con 01 y 02 recien ejecutados, la base contiene:
- Cabanas 1 a 4 (capacidad 2, 4, 4 y 6; tarifas $45000, $60000, $70000 y $95000).
- Reserva 1: cabana 2, 09/10 al 12/10/2026, 3 noches a $70000 = $210000, CONFIRMADA, con una
  sena de $70000 (saldo $140000). La tarifa aplicada histórica es $70000/noche, aunque la tarifa
  vigente de la cabana 2 es $60000; el importe se calcula con la tarifa guardada en la reserva.
- Reserva 2: cabana 4, 10/10 al 13/10/2026, CONFIRMADA.
- Reserva 3: cabana 1, 01/09 al 04/09/2026, FINALIZADA y pagada.
- Clientes: documento 30111222 (Maria Gonzalez) y 28555444 (Carlos Ruiz).
Con esos datos se reproducen los 18 casos de la Tabla 9. Cada caso parte de la base recien
cargada, salvo el 10 (check-out), que se ejecuta despues del check-in del caso 9, y el 17
(bitacora), que se ejecuta despues de los casos 3 y 5.
Para volver a ese estado despues de probar, ejecuta sql/03_reiniciar_demo.sql como administrador.
El script no borra la bitacora (es un archivo externo a MySQL); para repetir el caso 17 desde cero,
elimina manualmente salida/logs/operaciones.log.

EJEMPLO PARA LA DEMOSTRACION DEL MENU
Con la base de prueba cargada, puedes demostrar:
1. Opcion 1: registrar un cliente nuevo con un documento que no exista.
2. Opcion 2: consultar disponibilidad del 10/12/2026 al 12/12/2026 (las 4 cabanas libres).
3. Opcion 3: crear una reserva usando el documento del cliente, id de cabana 1, las mismas
   fechas y 2 huespedes (la cabana 1 admite 2).
4. Opcion 5: registrar una sena de 30000, tipo SENA, metodo EFECTIVO.
5. Opcion 6: consultar el saldo. Para dos noches en la cabana 1 ($45000 por noche), el total
   es $90000 y el saldo luego de la sena es $60000.
6. Opcion 9: generar el reporte del ano 2026 y mostrar la ruta del CSV.
7. Opcion 10: mostrar las ultimas operaciones de la bitacora.
Si repites la demostracion, ejecuta antes sql/03_reiniciar_demo.sql.

CARPETAS DE SALIDA
Al ejecutar el menu, el programa crea automaticamente la carpeta salida/ con:
- reportes/ocupacion_AAAA.csv
- comprobantes/reserva_ID.txt
- logs/operaciones.log

IMPORTANTE
- PruebaModelo verifica modelo, arreglos, ArrayList y archivos, pero no prueba operaciones
  reales en MySQL.
- Para mostrar altas, reservas, pagos, disponibilidad y reportes consultados desde MySQL,
  necesitas el servidor activo, la base preparada, el usuario configurado y Connector/J en lib.
- No publiques el archivo JAR de Connector/J dentro de GitHub si tu catedra no lo solicita;
  tampoco publiques contrasenas.
