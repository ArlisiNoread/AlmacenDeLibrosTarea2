package almacendelibrostarea2;

import clasesEnComun.Libro;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Cliente {

    private String servidor;
    private int puerto;

    public Cliente(String servidor, int puerto) {
        this.servidor = servidor;
        this.puerto = puerto;
    }

    public void iniciarCliente() {
        conectarConServidor();
    }

    public void conectarConServidor() {
        ManejoDeDatosCliente manejoDeDatosCliente = new ManejoDeDatosCliente();

        Socket socket = null;
        DataInputStream in = null;
        InputStream inputStream = null;
        OutputStream out = null;
        DataOutputStream dos = null;
        try {
            //Iniciando conexión con puerto
            socket = new Socket(this.servidor, this.puerto);
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            inputStream = socket.getInputStream();
            in = new DataInputStream(inputStream);
            
            ArrayList<Libro> arregloLibros = manejoDeDatosCliente.obtenerLibrosAMandar();
            String formatoParaMandar = manejoDeDatosCliente.obtenerFormatoParaMandar(arregloLibros);
            dos.writeUTF(formatoParaMandar);

            String mensaje = in.readUTF();
            System.out.println("Mensaje del Servidor: " + mensaje);

            dos.close();
            out.close();
            in.close();
            inputStream.close();
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void enviarLibros() {
        //Lee de un archivo de excel 
        //Lo manda al servidor
    }

}

class ManejoDeDatosCliente {

    private String direccionBase = "./carpetaCliente/";
    private String nombreArchivoBD = "archivoACopiar.xlsx";

    public ArrayList<Libro> obtenerLibrosAMandar() {
        ArrayList<Libro> arregloDeLibros = new ArrayList<>();
        File archivoAChecar = new File(this.archivo());

        if (!archivoAChecar.exists()) {
            System.out.println("Archivo cliente no encontrado");
            return arregloDeLibros;
        }

        try {
            FileInputStream inputStream = new FileInputStream(archivoAChecar);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();

            iterator.next(); //Avanzo una y evado nombres de columna
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                //Iterator<Cell> cellIterator = nextRow.cellIterator();
                //System.out.println(nextRow.getCell(5).getStringCellValue().trim().replaceAll(Pattern.quote("$"), ""));

                if (nextRow != null && nextRow.getCell(0) != null && !nextRow.getCell(0).getStringCellValue().equals("")) {
                    arregloDeLibros.add(new Libro(
                            nextRow.getCell(1).getStringCellValue(),
                            nextRow.getCell(2).getStringCellValue(),
                            nextRow.getCell(3).getStringCellValue(),
                            nextRow.getCell(4).getStringCellValue(),
                            Double.parseDouble(nextRow.getCell(5).getStringCellValue().trim().replaceAll(Pattern.quote("$"), "")))
                    );
                }

                /*
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.getCellType() == CellType.STRING) {
                        System.out.println(cell.getCellType() + " :" + cell.getStringCellValue());
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.println(cell.getCellType() + " :" + cell.getNumericCellValue());
                    }
                }
                 */
            }

            /*arregloDeLibros.forEach((n) -> {
                System.out.println(n);
            });*/
            workbook.close();
            inputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return arregloDeLibros;
    }

    public String obtenerFormatoParaMandar(ArrayList<Libro> arregloLibros) {
        //Genera una cadena que puedo tokenizar del otro lado
        String ret = "|";

        for (int x = 0; x < arregloLibros.size(); x++) {
            ret += arregloLibros.get(x).formatoParaMandar() + "|";
        }
        return ret;
    }

    private String archivo() {
        return this.direccionBase + this.nombreArchivoBD;
    }

}
