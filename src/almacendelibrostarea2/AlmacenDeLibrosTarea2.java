package almacendelibrostarea2;

/*
//  Tarea 2 por Roberto Manuel Piña Sevilla 2122002333.
//  Sencilla, no hay menú, sólo busca si hay un excel y lo manda al servidor.
//  El servidor arranca, el cliente conecta con server, hacen lo que deben hacer,
//  se apaga el servidor, se termina todo.
*/

public class AlmacenDeLibrosTarea2 {

    public static void main(String[] args) throws InterruptedException {
        //Ejecuto servidor
        new Server(1234).start();
        Thread.sleep(500);
        
        //Ejecuto cliente
        new Cliente("localhost", 1234).iniciarCliente();
    }

}


