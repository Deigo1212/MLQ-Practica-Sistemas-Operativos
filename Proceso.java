// Archivo: Proceso.java

public class Proceso {
    // --- Atributos de Entrada (leídos del archivo) ---
    String etiqueta;
    int burstTime;
    int arrivalTime;
    int nivelCola;
    int prioridad;

    // --- Atributos para la simulación y resultados ---
    int tiempoRestante;
    int tiempoInicio = -1; // -1 indica que no ha comenzado
    int completionTime;    // (CT) Tiempo de finalización
    int waitingTime;       // (WT) Tiempo de espera
    int turnaroundTime;    // (TAT) Tiempo de retorno
    int responseTime;      // (RT) Tiempo de respuesta

    // --- Constructor ---
    // Se usa para crear un nuevo objeto Proceso con los datos del archivo de texto.
    public Proceso(String etiqueta, int burst, int arrival, int cola, int prioridad) {
        this.etiqueta = etiqueta;
        this.burstTime = burst;
        this.arrivalTime = arrival;
        this.nivelCola = cola;
        this.prioridad = prioridad;
        
        // Al crearse, el tiempo que le falta es igual a su duración total.
        this.tiempoRestante = burst;
    }

    // --- Método para mostrar la información del proceso (opcional pero útil) ---
    // Te puede servir para hacer pruebas y depurar tu código.
    @Override
    public String toString() {
        return "Proceso{" +
                "etiqueta='" + etiqueta + '\'' +
                ", burstTime=" + burstTime +
                ", arrivalTime=" + arrivalTime +
                ", nivelCola=" + nivelCola +
                '}';
    }
}
