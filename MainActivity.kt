package com.example.cuartapp2026m

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabasOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DatabasOpenHelper(this)

        setContent {
            //pantalla del splash primero
            var showSplash by remember { mutableStateOf(true) }
            //si "showsplash" es true, mostramos el splashscreen
            if (showSplash){
                SplashScreen {
                    showSplash = false //cambia el estado despues del timeout
                }
            }else{
                //cuando el splash termina, mostramos la pantalla principal
                val navController = rememberNavController()

                // Requisito: Inicia en el formulario "principal" primero
                NavHost(navController = navController, startDestination = "principal?citaId=-1") {

                    // Pantalla 1: Ingreso / Edicion de Datos Personales
                    composable(
                        "principal?citaId={citaId}",
                        arguments = listOf(navArgument("citaId") { type = NavType.IntType; defaultValue = -1 })
                    ) { backStackEntry ->
                        val citaId = backStackEntry.arguments?.getInt("citaId") ?: -1
                        PrincipalScreen(navController, dbHelper, citaId)
                    }

                    // Pantalla 2: Seleccion de fecha y hora
                    composable(
                        "detalle/{nombre}/{telefono}?citaId={citaId}",
                        arguments = listOf(
                            navArgument("nombre") { type = NavType.StringType },
                            navArgument("telefono") { type = NavType.StringType },
                            navArgument("citaId") { type = NavType.IntType; defaultValue = -1 }
                        )
                    ) { backStackEntry ->
                        val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                        val telefono = backStackEntry.arguments?.getString("telefono") ?: ""
                        val citaId = backStackEntry.arguments?.getInt("citaId") ?: -1
                        DetalleScreen(nombre, telefono, navController, dbHelper, citaId)
                    }

                    // Pantalla 4: Resumen de Cita
                    composable(
                        "resumen/{nombre}/{telefono}/{fecha}/{hora}",
                        arguments = listOf(
                            navArgument("nombre") { type = NavType.StringType },
                            navArgument("telefono") { type = NavType.StringType },
                            navArgument("fecha") { type = NavType.StringType },
                            navArgument("hora") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                        val telefono = backStackEntry.arguments?.getString("telefono") ?: ""
                        val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
                        val hora = backStackEntry.arguments?.getString("hora") ?: ""
                        ResumenScreen(nombre, telefono, fecha, hora, navController)
                    }

                    // Pantalla 3: Lista de citas
                    composable("lista") {
                        ListaCitasScreen(navController, dbHelper)
                    }
                }
            }
        }
    }

    @Composable
    fun SplashScreen(onTimeout: () -> Unit){
        //controla el temporizador de la pantalla de inicio
        LaunchedEffect(Unit){
            delay(3000) // 3 segundos de splash
            onTimeout()
        }
        //diseño de la pantalla de splash
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), //icono del logo
                    contentDescription = "Splash Logo",
                    modifier = Modifier.size(150.dp)//ajusta el tamaño del logo
                )
            }
        }
    }
}

// === PANTALLA 1: INGRESO / EDICION DE DATOS PERSONALES ===
@Composable
fun PrincipalScreen(navController: NavController, dbHelper: DatabasOpenHelper, citaId: Int) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var telefonoError by remember { mutableStateOf(false) }

    LaunchedEffect(citaId) {
        if (citaId != -1) {
            val cita = dbHelper.getCitaById(citaId)
            if (cita != null) {
                nombre = cita.nombre
                telefono = cita.telefono
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = if (citaId == -1) "Ingrese sus datos personales" else "Modificar datos personales",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    nombreError = it.isBlank()
                },
                label = { Text("Nombre Completo") },
                isError = nombreError,
                modifier = Modifier.fillMaxWidth()
            )
            if (nombreError) {
                Text("El nombre no puede estar vacio", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 10) {
                        telefono = input
                    }
                    telefonoError = telefono.length != 10
                },
                label = { Text("Telefono (10 digitos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = telefonoError,
                modifier = Modifier.fillMaxWidth()
            )
            if (telefonoError) {
                Text("El telefono debe tener exactamente 10 digitos", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Si venimos desde la lista (edicion), mostramos opcion de cancelar
                if (citaId != -1) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Cancelar")
                    }
                }
                Button(
                    onClick = {
                        nombreError = nombre.isBlank()
                        telefonoError = telefono.length != 10

                        if (!nombreError && !telefonoError) {
                            navController.navigate("detalle/$nombre/$telefono?citaId=$citaId")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Continuar")
                }
            }

            // Botón rapido para saltar directo a ver el historial de citas si no estamos editando
            if (citaId == -1) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.navigate("lista") }) {
                    Text("Ver citas agendadas")
                }
            }
        }
    }
}

// === PANTALLA 2: SELECCIoN DE FECHA Y HORA ===
@Composable
fun DetalleScreen(
    nombre: String,
    telefono: String,
    navController: NavController,
    dbHelper: DatabasOpenHelper,
    citaId: Int
) {
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var horaSeleccionada by remember { mutableStateOf(LocalTime.now()) }
    val context = LocalContext.current

    LaunchedEffect(citaId) {
        if (citaId != -1) {
            dbHelper.getCitaById(citaId)?.let {
                fechaSeleccionada = LocalDate.parse(it.fecha)
                horaSeleccionada = LocalTime.parse(it.hora)
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text("Seleccione la fecha y la hora",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("Paciente: $nombre", style = MaterialTheme.typography.bodyLarge)
            Text("Telefono: $telefono", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            DatePickerDialogExample(fechaSeleccionada) { fechaSeleccionada = it }
            Spacer(modifier = Modifier.height(16.dp))
            TimePickerDialogExample(horaSeleccionada) { horaSeleccionada = it }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Fecha: ${fechaSeleccionada.format(DateTimeFormatter.ISO_LOCAL_DATE)}", style = MaterialTheme.typography.bodyMedium)
            Text("Hora: ${horaSeleccionada.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val fechaString = fechaSeleccionada.toString()
                    val horaString = horaSeleccionada.toString()
                    val nuevaCita = Cita(id = citaId, nombre = nombre, telefono = telefono, fecha = fechaString, hora = horaString)

                    val exito = if (citaId == -1) {
                        dbHelper.insertCita(nuevaCita)
                    } else {
                        dbHelper.updateCita(nuevaCita)
                    }

                    if (exito) {
                        Toast.makeText(context, if (citaId == -1) "Cita Guardada" else "Cita Actualizada", Toast.LENGTH_SHORT).show()
                        navController.navigate("resumen/$nombre/$telefono/$fechaString/$horaString") {
                            // Al confirmar, limpiamos el stack hasta la raíz para evitar loops al pulsar atrás
                            popUpTo("principal?citaId=-1") { inclusive = (citaId == -1) }
                        }
                    } else {
                        Toast.makeText(context, "Error al procesar la Base de Datos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (citaId == -1) "Confirmar Cita" else "Guardar Cambios")
            }
        }
    }
}

// === PANTALLA 4: RESUMEN DE LA CITA ===
@Composable
fun ResumenScreen(nombre: String, telefono: String, fecha: String, hora: String, navController: NavController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text("Resumen de la Cita", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nombre: $nombre", style = MaterialTheme.typography.bodyLarge)
            Text("Telefono: $telefono", style = MaterialTheme.typography.bodyLarge)
            Text("Fecha de la Cita: $fecha", style = MaterialTheme.typography.bodyLarge)
            Text("Hora de la Cita: $hora", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    // Requisito: Desde el resumen avanzamos directamente a ver el listado total de citas
                    navController.navigate("lista") {
                        popUpTo("principal?citaId=-1") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Todas las Citas")
            }
        }
    }
}

// === PANTALLA 3: LISTA DE CITAS (APARECE AL FINAL / ÚLTIMA) ===
@Composable
fun ListaCitasScreen(navController: NavController, dbHelper: DatabasOpenHelper) {
    var listaCitas by remember { mutableStateOf(listOf<Cita>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        listaCitas = dbHelper.getAllCitas()
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 32.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Citas Agendadas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (listaCitas.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay citas registradas.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(listaCitas) { cita ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Paciente: ${cita.nombre}", style = MaterialTheme.typography.titleMedium)
                                Text("Telefono: ${cita.telefono}", style = MaterialTheme.typography.bodyMedium)
                                Text("Fecha: ${cita.fecha}  |  Hora: ${cita.hora}", style = MaterialTheme.typography.bodyMedium)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { navController.navigate("principal?citaId=${cita.id}") },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Editar")
                                    }
                                    Button(
                                        onClick = {
                                            if (dbHelper.deleteCita(cita.id)) {
                                                Toast.makeText(context, "Cita eliminada correctamente", Toast.LENGTH_SHORT).show()
                                                listaCitas = dbHelper.getAllCitas()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate("principal?citaId=-1") {
                        popUpTo("lista") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Volver a Agendar otra Cita")
            }
        }
    }
}

// === DIALOGS AUXILIARES ===
@Composable
fun DatePickerDialogExample(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    Button(onClick = { datePickerDialog.show() }) {
        Text("Seleccionar Fecha")
    }
}

@Composable
fun TimePickerDialogExample(selectedTime: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        selectedTime.hour,
        selectedTime.minute,
        false
    )

    Button(onClick = { timePickerDialog.show() }) {
        Text("Seleccionar Hora")
    }
}