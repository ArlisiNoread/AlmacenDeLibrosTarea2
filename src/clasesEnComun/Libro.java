package clasesEnComun;

public class Libro {

    String nombre, autor, tipo, descripcion;
    double precio;

    public Libro(String nombre, String autor, String tipo, String descripcion, double precio) {
        this.nombre = nombre;
        this.autor = autor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String formatoParaMandar() {
        return this.nombre + "/" + this.autor + "/" + this.tipo + "/" + this.descripcion + "/" + this.precio;
    }

    @Override
    public String toString() {
        return "Libro{" + "nombre=" + nombre + ", autor=" + autor + ", tipo=" + tipo + ", descripcion=" + descripcion + ", precio=" + precio + '}';
    }

    public String getNombre() {
        return nombre;
    }

    public String getAutor() {
        return autor;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }
    
    
}
