// Define el paquete al que pertenece este archivo. Ayuda a organizar el código.
package com.csanchez.actividad02

// Importa las clases necesarias de Android y otras bibliotecas.
// Es como "traer" herramientas que necesitas para construir tu aplicación.
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Define una "clase de datos" para representar una tarea.
// Una clase de datos es una forma sencilla de crear una clase que solo contiene datos.
data class Task(val id: Int, val title: String, var isCompleted: Boolean = false) {
    // Sobrescribe el método `toString` para que, cuando imprimamos una tarea, solo muestre el título.
    override fun toString(): String {
        return title
    }
}

// Define una "interfaz", que es como un contrato.
// Cualquier clase que "implemente" esta interfaz debe proporcionar el código para estas funciones.
interface TaskOperations {
    fun addTask(title: String): Task // Función para agregar una nueva tarea.
    fun getTasks(): List<Task>       // Función para obtener todas las tareas.
    fun completeTask(taskId: Int): Boolean // Función para marcar una tarea como completada.
    fun getCompletedTasks(): List<Task> // Función para obtener solo las tareas completadas.
}

// Crea un "objeto" llamado `TaskManager` que implementa la interfaz `TaskOperations`.
// Un "objeto" es una clase de la que solo puede haber una instancia (un "singleton").
object TaskManager : TaskOperations {
    // Una lista "mutable" (que puede cambiar) para almacenar todas las tareas. Es "privada", lo que significa que solo se puede acceder desde dentro de `TaskManager`.
    private val tasks = mutableListOf<Task>()
    // Un contador para el ID de la siguiente tarea. También es privado.
    private var nextId = 1

    // Implementación de la función `addTask` del contrato `TaskOperations`.
    override fun addTask(title: String): Task {
        // Crea una nueva tarea con un ID único.
        val task = Task(nextId++, title)
        // Agrega la nueva tarea a la lista.
        tasks.add(task)
        // Devuelve la tarea recién creada.
        return task
    }

    // Implementación de la función `getTasks`.
    override fun getTasks(): List<Task> {
        // Devuelve una copia de solo lectura de la lista de tareas.
        return tasks.toList()
    }

    // Implementación de la función `completeTask`.
    override fun completeTask(taskId: Int): Boolean {
        // Usa un bloque "try-catch" para manejar posibles errores.
        return try {
            // Busca la primera tarea en la lista que tenga el ID proporcionado.
            val task = tasks.firstOrNull { it.id == taskId }
            // Si se encuentra una tarea...
            task?.let {
                // ...marca la tarea como completada.
                it.isCompleted = true
                // Devuelve "true" para indicar que la operación fue exitosa.
                return true
            }
            // Si no se encuentra ninguna tarea, devuelve "false".
            false
        } catch (e: Exception) {
            // Si ocurre un error, imprime un mensaje de error en la consola.
            println("Error al completar la tarea: ${e.message}")
            // Devuelve "false" para indicar que la operación falló.
            false
        }
    }

    // Implementación de la función `getCompletedTasks`.
    override fun getCompletedTasks(): List<Task> {
        // "Filtra" la lista de tareas y devuelve solo las que están completadas.
        return tasks.filter { it.isCompleted }
    }
}

// La clase principal de la aplicación. Se ejecuta cuando se inicia la aplicación.
class MainActivity : AppCompatActivity() {
    // Esta función se llama cuando se crea la "Actividad" (la pantalla de la aplicación).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita el modo de borde a borde para que la aplicación ocupe toda la pantalla.
        enableEdgeToEdge()
        // Establece el diseño de la interfaz de usuario de la actividad desde el archivo `activity_main.xml`.
        setContentView(R.layout.activity_main)
        // Configura el relleno de la vista principal para evitar que el contenido se superpone con las barras del sistema (como la barra de estado).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Aquí es donde usamos nuestro TaskManager ---

        // Agrega tres tareas de ejemplo.
        TaskManager.addTask("Comprar leche")
        TaskManager.addTask("Pasear al perro")
        TaskManager.addTask("Hacer la cama")
        // Completa la primera y la tercera tarea.
        TaskManager.completeTask(1)
        TaskManager.completeTask(3)

        // Actualiza la vista de tareas.
        updateTasksView()

        // Encuentra el FloatingActionButton por su ID.
        val addTaskFab = findViewById<FloatingActionButton>(R.id.add_task_fab)

        // Configura un listener para manejar los clics en el botón.
        addTaskFab.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        // Crea un constructor para un cuadro de diálogo de alerta.
        // 'this' se refiere a la actividad actual (MainActivity).
        val builder = AlertDialog.Builder(this)
        
        // Establece el título que se mostrará en la parte superior del diálogo.
        builder.setTitle("Agregar Tarea")

        // Crea un nuevo campo de texto (EditText) mediante programación.
        val input = EditText(this)
        
        // Establece un texto de sugerencia que aparece en el campo de texto cuando está vacío.
        input.hint = "Escribe el título de la tarea"
        
        // Agrega el campo de texto (input) a la vista del cuadro de diálogo.
        builder.setView(input)

        // Configura el botón "positivo" del diálogo (generalmente, el botón de "Aceptar" o "Agregar").
        builder.setPositiveButton("Agregar") { dialog, _ ->
            // Obtiene el texto introducido por el usuario en el campo 'input' y lo convierte a una cadena de texto.
            val taskTitle = input.text.toString()
            
            // Comprueba si el título de la tarea no está en blanco (no es solo espacios vacíos).
            if (taskTitle.isNotBlank()) {
                // Si no está en blanco, agrega la nueva tarea usando el TaskManager.
                TaskManager.addTask(taskTitle)
                // Actualiza la vista de la lista de tareas en la pantalla principal.
                updateTasksView()
                // Muestra un mensaje corto (Toast) para confirmar que la tarea fue agregada.
                Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()
            } else {
                // Si el título está en blanco, muestra un mensaje de error.
                Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            // Cierra el cuadro de diálogo.
            dialog.dismiss()
        }
        
        // Configura el botón "negativo" del diálogo (generalmente, el botón de "Cancelar").
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            // Cierra el cuadro de diálogo sin hacer nada.
            dialog.cancel()
        }

        // Crea y muestra el cuadro de diálogo que acabamos de configurar.
        builder.show()
    }

    private fun updateTasksView() {
        // Obtiene la lista de todas las tareas y la lista de tareas completadas.
        val allTasks = TaskManager.getTasks()
        val completedTasks = TaskManager.getCompletedTasks()

        // Obtiene una referencia a los elementos `TextView` del diseño.
        val allTasksTextView = findViewById<TextView>(R.id.all_tasks_textview)
        val completedTasksTextView = findViewById<TextView>(R.id.completed_tasks_textview)

        // Convierte las listas de tareas en una sola cadena de texto, con cada tarea en una nueva línea.
        val allTasksText = allTasks.joinToString(separator = "\n")
        val completedTasksText = completedTasks.joinToString(separator = "\n")

        // Muestra el texto en los `TextView` correspondientes.
        allTasksTextView.text = allTasksText
        completedTasksTextView.text = completedTasksText
    }
}