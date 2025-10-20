// Se importan las herramientas de Java que vamos a necesitar.
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// Juan Diego Ledezma 
// Codigo 2540088-3743
public class SimuladorMLQ {

    // --- Colas de Planificación ---
    // Usamos la interfaz Queue y la implementación LinkedList, que funciona como una fila.
    private Queue<Proceso> colaAltaPrioridad;   // Para Q=1 (RR q=3)
    private Queue<Proceso> colaMediaPrioridad;  // Para Q=2 (RR q=5)
    private Queue<Proceso> colaBajaPrioridad;   // Para Q=3 (FCFS)

    // --- Listas para Administrar Procesos ---
    // Guarda los procesos leídos del archivo antes de que lleguen al sistema.
    private List<Proceso> procesosNuevos;
    // Guarda los procesos que ya terminaron para después calcular métricas.
    private List<Proceso> procesosTerminados;

    // --- Estado de la Simulación ---
    private int tiempoActual;

    // --- Constructor ---
    // Se ejecuta al crear un 'new SimuladorMLQ()'. Prepara todo para empezar.
    public SimuladorMLQ() {
        // Se inicializan las colas y listas como contenedores vacíos.
        this.colaAltaPrioridad = new LinkedList<>();
        this.colaMediaPrioridad = new LinkedList<>();
        this.colaBajaPrioridad = new LinkedList<>();
        this.procesosNuevos = new ArrayList<>();
        this.procesosTerminados = new ArrayList<>();
        this.tiempoActual = 0; // El tiempo siempre empieza en cero.
    }

    // --- Método para leer el archivo de entrada ---
    public void cargarProcesos(String nombreArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("#") || linea.trim().isEmpty()) {
                    continue; // Ignora comentarios y líneas vacías.
                }

                // Separa la línea por ";" y quita espacios extra.
                String[] partes = linea.split(";");
                String etiqueta = partes[0].trim();
                int burstTime = Integer.parseInt(partes[1].trim());
                int arrivalTime = Integer.parseInt(partes[2].trim());
                int nivelCola = Integer.parseInt(partes[3].trim());
                int prioridad = Integer.parseInt(partes[4].trim());

                // Crea el objeto Proceso y lo añade a la lista de espera.
                this.procesosNuevos.add(new Proceso(etiqueta, burstTime, arrivalTime, nivelCola, prioridad));
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    // Archivo: SimuladorMLQ.java

    // ... (aquí va el método cargarProcesos que ya tienes)

    // --- El Motor Principal de la Simulación ---
    public void ejecutarSimulacion() {
        System.out.println("\n--- Iniciando Simulación ---");

        // El bucle principal continuará mientras haya procesos por llegar
        // o mientras alguna de las colas de listos no esté vacía.
        while (!procesosNuevos.isEmpty() || !colaAltaPrioridad.isEmpty() || !colaMediaPrioridad.isEmpty() || !colaBajaPrioridad.isEmpty()) {
            
            // --- PASO 1: Mover procesos nuevos a las colas de listos ---
            // Revisa si algún proceso de la lista 'procesosNuevos' debe entrar al sistema en el 'tiempoActual'.
            List<Proceso> procesosQueLlegan = new ArrayList<>();
            for (Proceso p : procesosNuevos) {
                if (p.arrivalTime <= this.tiempoActual) {
                    // Si el proceso llega ahora, lo movemos a la cola que le corresponde.
                    switch (p.nivelCola) {
                        case 1:
                            colaAltaPrioridad.add(p);
                            break;
                        case 2:
                            colaMediaPrioridad.add(p);
                            break;
                        case 3:
                            colaBajaPrioridad.add(p);
                            break;
                    }
                    procesosQueLlegan.add(p);
                }
            }
            // Quitamos los procesos que ya ingresaron de la lista de 'procesosNuevos'.
            procesosNuevos.removeAll(procesosQueLlegan);


            // --- PASO 2: Lógica de Planificación (qué proceso ejecutar) ---
            Proceso procesoEnEjecucion = null;

            if (!colaAltaPrioridad.isEmpty()) {
                // Lógica para RR (Quantum = 3)
                procesoEnEjecucion = colaAltaPrioridad.poll(); // Saca el proceso de la cabeza de la cola
                System.out.println("Tiempo " + tiempoActual + ": Ejecutando " + procesoEnEjecucion.etiqueta + " de la cola de alta prioridad.");

                // Registrar la primera vez que se ejecuta para el Response Time
                if (procesoEnEjecucion.tiempoInicio == -1) {
                    procesoEnEjecucion.tiempoInicio = tiempoActual;
                    procesoEnEjecucion.responseTime = procesoEnEjecucion.tiempoInicio - procesoEnEjecucion.arrivalTime;
                }

                // El proceso se ejecuta por un quantum de 3, o menos si le falta poco para terminar.
                int tiempoAEjecutar = Math.min(procesoEnEjecucion.tiempoRestante, 3);
                procesoEnEjecucion.tiempoRestante -= tiempoAEjecutar;
                
                // Avanzamos el tiempo global por la cantidad que se ejecutó
                tiempoActual += tiempoAEjecutar;

                if (procesoEnEjecucion.tiempoRestante > 0) {
                    // Si no ha terminado, regresa al final de su cola
                    colaAltaPrioridad.add(procesoEnEjecucion);
                } else {
                    // Si terminó, se calcula su finalización y se mueve a la lista de terminados
                    procesoEnEjecucion.completionTime = tiempoActual;
                    procesosTerminados.add(procesoEnEjecucion);
                    System.out.println(" -> Proceso " + procesoEnEjecucion.etiqueta + " TERMINADO.");
                }

            } else if (!colaMediaPrioridad.isEmpty()) {
                // Lógica para RR (Quantum = 5) - similar a la anterior
                procesoEnEjecucion = colaMediaPrioridad.poll();
                System.out.println("Tiempo " + tiempoActual + ": Ejecutando " + procesoEnEjecucion.etiqueta + " de la cola de media prioridad.");

                if (procesoEnEjecucion.tiempoInicio == -1) {
                    procesoEnEjecucion.tiempoInicio = tiempoActual;
                    procesoEnEjecucion.responseTime = procesoEnEjecucion.tiempoInicio - procesoEnEjecucion.arrivalTime;
                }
                
                int tiempoAEjecutar = Math.min(procesoEnEjecucion.tiempoRestante, 5);
                procesoEnEjecucion.tiempoRestante -= tiempoAEjecutar;
                tiempoActual += tiempoAEjecutar;

                if (procesoEnEjecucion.tiempoRestante > 0) {
                    colaMediaPrioridad.add(procesoEnEjecucion);
                } else {
                    procesoEnEjecucion.completionTime = tiempoActual;
                    procesosTerminados.add(procesoEnEjecucion);
                    System.out.println(" -> Proceso " + procesoEnEjecucion.etiqueta + " TERMINADO.");
                }

            } else if (!colaBajaPrioridad.isEmpty()) {
                // Lógica para FCFS
                procesoEnEjecucion = colaBajaPrioridad.poll(); // Saca el proceso
                System.out.println("Tiempo " + tiempoActual + ": Ejecutando " + procesoEnEjecucion.etiqueta + " de la cola de baja prioridad.");

                 if (procesoEnEjecucion.tiempoInicio == -1) {
                    procesoEnEjecucion.tiempoInicio = tiempoActual;
                    procesoEnEjecucion.responseTime = procesoEnEjecucion.tiempoInicio - procesoEnEjecucion.arrivalTime;
                }

                // En FCFS, se ejecuta hasta que termina
                int tiempoAEjecutar = procesoEnEjecucion.tiempoRestante;
                procesoEnEjecucion.tiempoRestante = 0;
                tiempoActual += tiempoAEjecutar;
                
                procesoEnEjecucion.completionTime = tiempoActual;
                procesosTerminados.add(procesoEnEjecucion);
                System.out.println(" -> Proceso " + procesoEnEjecucion.etiqueta + " TERMINADO.");

            } else {
                // Si todas las colas están vacías, la CPU está inactiva.
                // Simplemente avanzamos el tiempo.
                System.out.println("Tiempo " + tiempoActual + ": CPU Inactiva.");
                tiempoActual++;
            }

            // --- PASO 3: Avanzar el tiempo ---
        
        }

        System.out.println("--- Simulación Terminada en tiempo " + this.tiempoActual + " ---");
    }
    // --- Punto de Entrada del Programa ---

    public void generarReporte(String nombreArchivo) {
        // Variables para sumar los totales y luego calcular los promedios
        double totalWT = 0;
        double totalCT = 0;
        double totalRT = 0;
        double totalTAT = 0;

        System.out.println("\n--- Generando Reporte ---");

        // Usamos try-catch para manejar posibles errores al escribir el archivo
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo))) {
            
            // Escribimos la cabecera del archivo de salida
            pw.println("#etiqueta; BT; AT; Q; Pr; WT; CT; RT; TAT");

            // Iteramos sobre cada proceso que terminó
            for (Proceso p : this.procesosTerminados) {
                // Calculamos las métricas que faltan
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;

                // Sumamos los valores para los promedios
                totalWT += p.waitingTime;
                totalCT += p.completionTime;
                totalRT += p.responseTime;
                totalTAT += p.turnaroundTime;

                // Creamos la línea de texto para el archivo con el formato exacto
                String linea = String.format("%s;%d;%d;%d;%d;%d;%d;%d;%d",
                        p.etiqueta, p.burstTime, p.arrivalTime, p.nivelCola, p.prioridad,
                        p.waitingTime, p.completionTime, p.responseTime, p.turnaroundTime);
                
                // Escribimos la línea en el archivo
                pw.println(linea);
            }

            // Calculamos los promedios
            int numProcesos = this.procesosTerminados.size();
            double avgWT = totalWT / numProcesos;
            double avgCT = totalCT / numProcesos;
            double avgRT = totalRT / numProcesos;
            double avgTAT = totalTAT / numProcesos;

            // Escribimos la línea final con los promedios
            String promedios = String.format("$WT=%.2f$; $CT=%.2f$ $RT=%.2f$; $TAT=%.2f$;",
                                              avgWT, avgCT, avgRT, avgTAT);
            pw.println(promedios);

            System.out.println("Reporte '" + nombreArchivo + "' generado exitosamente.");

        } catch (IOException e) {
            System.err.println("Error al escribir el archivo de reporte: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        SimuladorMLQ simulador = new SimuladorMLQ();

        // 1. Cargar los procesos.
        simulador.cargarProcesos("mlq004.txt");
        System.out.println(simulador.procesosNuevos.size() + " procesos cargados.");

        // 2. Ejecutar la simulación.
        simulador.ejecutarSimulacion(); // <-- AÑADE ESTA LÍNEA

        // 3. (Paso final) Generar el reporte.
         simulador.generarReporte("salida_mlq004.txt");
    }

}
