package almacendelibrostarea2;

import clasesEnComun.Libro;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import static org.apache.poi.hssf.usermodel.HeaderFooter.file;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

public class Server extends Thread {

    int puerto;

    public Server(int puerto) {
        this.puerto = puerto;
    }

    public void control(Socket socket) {
        OutputStream out = null;
        DataOutputStream dos = null;
        DataInputStream in = null;
        InputStream inputStream = null;
        try {
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            inputStream = socket.getInputStream();
            in = new DataInputStream(inputStream);

            ManejoDeDatosServer manejoDeDatos = new ManejoDeDatosServer();
            manejoDeDatos.inicializarBDSiNoExiste();
            String mensaje = in.readUTF();
            ArrayList<Libro> arrayLibros = manejoDeDatos.reorganizaStringAArray(mensaje);
            manejoDeDatos.insertarLibrosEnPersistencia(arrayLibros);

            dos.writeUTF("Libros Insertados");

            //Cerrando conexiones
            in.close();
            inputStream.close();
            dos.close();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Inicializando Servidor");
        ServerSocket server = null;
        try {
            //Iniciando servidor
            server = new ServerSocket(this.puerto);
            System.out.println("Servidor ejecutándose y en espera");
            Socket socket = server.accept();
            this.control(socket);

            //Cerrando socket
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("Servidor finalizado");
        }
    }

}

class ManejoDeDatosServer {

    private String direccionBase = "./carpetaServidor/";
    private String nombreArchivoBD = "archivoBD.xlsx";

    public void insertarLibrosEnPersistencia(ArrayList<Libro> libros) {
        Libro libro;
        for (int x = 0; x < libros.size(); x++) {
            libro = libros.get(x);
            if (!estaLibroYaEnPersistencia(libro)) {
                this.insertarLibroAlFinal(libro);
            }

        }
    }

    private void insertarLibroAlFinal(Libro libro) {
        InputStream inp;
        try {
            inp = new FileInputStream(this.archivo());
            Workbook wb = XSSFWorkbookFactory.create(inp);
            inp.close();
            Sheet myExcelSheet = wb.getSheet("Almacen De Libros");
            Row row = myExcelSheet.createRow(myExcelSheet.getPhysicalNumberOfRows());
            Cell c = row.createCell(0);
            c.setCellValue(libro.getNombre());
            c = row.createCell(1);
            c.setCellValue(libro.getAutor());
            c = row.createCell(2);
            c.setCellValue(libro.getTipo());
            c = row.createCell(3);
            c.setCellValue(libro.getDescripcion());
            c = row.createCell(4);
            c.setCellValue(libro.getPrecio());
            FileOutputStream fileOut = new FileOutputStream(this.archivo());
            wb.write(fileOut);
            fileOut.close();
            wb.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ManejoDeDatosServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManejoDeDatosServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean estaLibroYaEnPersistencia(Libro libro) {
        try {

            Workbook myExcelBook = new XSSFWorkbook(new FileInputStream(this.archivo()));
            Sheet myExcelSheet = myExcelBook.getSheet("Almacen De Libros");
            Row row = myExcelSheet.getRow(1);
            if (row == null) {
                myExcelBook.close();
                return false;
            }

            int x = 1;
            while (row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getCellType() != CellType.BLANK && row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getCellType() != CellType._NONE) {

                if (row.getCell(0).getCellType() == CellType.STRING) {
                    String name = row.getCell(0).getStringCellValue();
                    if (name.equals(libro.getNombre())) {
                        myExcelBook.close();
                        return true;
                    }
                }
                row = myExcelSheet.getRow(x);
                if (row == null) {
                    myExcelBook.close();
                    return false;
                }
                x++;
            }

            myExcelBook.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(ManejoDeDatosServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public void inicializarBDSiNoExiste() {
        if (new File(this.archivo()).exists()) {
            return;
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Almacen De Libros");
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);

        Object[][] bookData = {
            {"Nombre", "Autor", "Tipo", "Descripción", "Precio"}
        };

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                cell.setCellStyle(cellStyle);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                } else if (field instanceof Double) {
                    cell.setCellValue((Double) field);
                }
            }

        }

        try (FileOutputStream outputStream = new FileOutputStream(this.archivo())) {
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ManejoDeDatosServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManejoDeDatosServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String archivo() {

        return this.direccionBase + this.nombreArchivoBD;
    }

    public ArrayList<Libro> reorganizaStringAArray(String cadena) {
        ArrayList<Libro> arregloLibros = new ArrayList<>();

        StringTokenizer tokenLibro = new StringTokenizer(cadena, "|");
        while (tokenLibro.hasMoreTokens()) {
            String stringLibro = tokenLibro.nextToken();

            StringTokenizer tokenClases = new StringTokenizer(stringLibro, "/");

            arregloLibros.add(new Libro(tokenClases.nextToken(),
                    tokenClases.nextToken(),
                    tokenClases.nextToken(),
                    tokenClases.nextToken(),
                    Double.parseDouble(tokenClases.nextToken())));
        }

        return arregloLibros;
    }

}
