# Historias de Usuario – Sistema PAGAPP

## HU1 – Registro de usuario

**Como** usuario nuevo  
**Quiero** registrarme en la aplicación  
**Para** poder acceder a las funcionalidades del sistema  

### Criterios de aceptación
- El usuario puede ingresar nombre, correo y contraseña.
- El sistema valida que el correo no esté registrado.
- El sistema guarda la información del usuario en la base de datos.
- El sistema confirma que el registro fue exitoso.

---

## HU2 – Inicio de sesión

**Como** usuario registrado  
**Quiero** iniciar sesión en la aplicación  
**Para** acceder a mi información financiera y mis deudas  

### Criterios de aceptación
- El usuario puede ingresar correo y contraseña.
- El sistema valida las credenciales.
- Si las credenciales son correctas, el sistema permite el acceso.
- Si son incorrectas, el sistema muestra un mensaje de error.

---

## HU3 – Ver panel principal

**Como** usuario  
**Quiero** visualizar un panel principal  
**Para** ver mi balance, deudas pendientes y actividad reciente  

### Criterios de aceptación
- El sistema muestra el balance total del usuario.
- Se visualizan las deudas pendientes.
- Se muestra la actividad reciente.
- La información se actualiza automáticamente.

---

## HU4 – Registrar gasto

**Como** usuario  
**Quiero** registrar un gasto compartido  
**Para** llevar control de los pagos entre amigos  

### Criterios de aceptación
- El usuario puede ingresar el monto del gasto.
- El usuario puede seleccionar a los amigos involucrados.
- El sistema divide el gasto entre los participantes.
- El sistema registra el gasto en el historial.

---

## HU5 – Consultar deudas pendientes

**Como** usuario  
**Quiero** ver las deudas pendientes  
**Para** saber cuánto debo y cuánto me deben  

### Criterios de aceptación
- El sistema muestra una lista de deudas.
- Se identifica quién debe pagar.
- Se muestra el monto de cada deuda.
- Se diferencia entre deudas por pagar y por recibir.

---

## HU6 – Pagar deuda

**Como** usuario  
**Quiero** registrar el pago de una deuda  
**Para** actualizar mi balance y el estado de la deuda  

### Criterios de aceptación
- El usuario puede seleccionar la deuda a pagar.
- El usuario puede confirmar el pago.
- El sistema actualiza el estado de la deuda.
- El sistema registra la transacción.

---

## HU7 – Ver historial de transacciones

**Como** usuario  
**Quiero** ver el historial de mis transacciones  
**Para** consultar gastos y pagos anteriores  

### Criterios de aceptación
- El sistema muestra una lista de transacciones.
- Cada transacción incluye fecha, monto y descripción.
- El usuario puede identificar ingresos y gastos.
- El historial se ordena por fecha.

---

## HU8 – Recibir notificaciones

**Como** usuario  
**Quiero** recibir notificaciones  
**Para** estar informado sobre nuevos gastos o pagos  

### Criterios de aceptación
- El sistema envía una notificación cuando se registra un gasto.
- El sistema envía una notificación cuando se paga una deuda.
- El usuario puede visualizar las notificaciones dentro de la aplicación.

---

## HU9 – Gestionar perfil

**Como** usuario  
**Quiero** gestionar mi perfil  
**Para** actualizar mi información personal  

### Criterios de aceptación
- El usuario puede editar su nombre.
- El usuario puede cambiar su foto de perfil.
- El usuario puede actualizar su correo electrónico.
- El sistema guarda los cambios realizados.

---

## HU10 – Detectar amigos cercanos

**Como** usuario  
**Quiero** detectar amigos cercanos mediante ubicación  
**Para** facilitar el registro de gastos compartidos  

### Criterios de aceptación
- El sistema obtiene la ubicación del usuario.
- El sistema detecta amigos cercanos.
- El usuario puede seleccionar un amigo cercano para registrar un gasto.
