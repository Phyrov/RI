import java.io.File;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.langdetect.optimaize.*;
import java.io.IOException;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class App {

    public static void main(String[] args) throws Exception {

        //Recogemos el nombre del directorio
        File carpeta = new File(args[0]);
        // Almacenamos en un array de files los diferentes ficheros
        File[] listado = carpeta.listFiles();

        // Si no hay ficheros
        if(listado == null || listado.length == 0) {
            System.out.println("No hay elementos dentro de la carpeta actual");
            return;
        
        // Si los hay
        }else{
            System.out.println("Por favor ingrese la opcion: \n -d Nombre,tipo,encoding y lengua \n -l Links \n -t Frecuencia de palabras");

            // Leemos la opcion pasada por teclado
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String opcion = br.readLine();

            // Estos arraylist los usaremos para operar la opcion del menú
            ArrayList <String> tipos = new ArrayList <String>();
            ArrayList <String> nombres = new ArrayList <String>();
            ArrayList <String> idiomas = new ArrayList <String>();
            ArrayList <String> links = new ArrayList <String>();
            ArrayList <String> arrayEncoding = new ArrayList <String>();
            ArrayList <String> textos = new ArrayList <String>();


            for (int i=0; i< listado.length; i++) {
                //System.out.println("----------------------------------");
                //System.out.println("Nombre: " + listado[i].getName());

                //Añadimos los nombres de los archivos al array
                nombres.add(listado[i].getName());
            
                // Creamos una instancia de Tika con la configuracion por defecto
                Tika tika = new Tika();
                // Nos quedamos con el fichero
                File f = listado[i];
                // Tipo de archivo. Según sea el tipo, utilizaremos un parser distinto
                String type = tika.detect(f);
                // Recogemos el texto del documento. Utilizamos este método porque el BodyContentHandler 
                // no procesa más de 10000 caracteres
                String text = tika.parseToString(f); 

                // Dependiendo del tipo, utilizamos un parser u otro
                switch(type){

                    case "application/pdf":
                        //System.out.println("Tipo: " + type);
                        // Añadimos el tipo al array de los tipos
                        tipos.add(type);
                        InputStream ficheroInput = new FileInputStream(f);
                        // Creamos los 3 handler para almacenar lo correspondiente
                        //BodyContentHandler textHandler = new BodyContentHandler();
                        LinkContentHandler linkHandler = new LinkContentHandler();
                        // El teeContentHandler es un contenedor de handler
                        //TeeContentHandler teeHandler = new TeeContentHandler(linkHandler);
                        // Creamos instancia de metadata
                        Metadata metadata = new Metadata();
                        // Creamos instancia de contexto
                        ParseContext pcontext = new ParseContext();

                        // Creamos instancia del parseador de pdf
                        PDFParser pdfparser = new PDFParser();
                        // Analizamos el fichero y guardamos lo que corresponde en cada handler
                        pdfparser.parse(ficheroInput,linkHandler,metadata,pcontext);
        
                        //System.out.println("Contenido: " + textHandler.toString());
        
                        // Llamamos a la función encargada de sacar el tipo de coding
                        String encoding = guessEncoding(ficheroInput);
                        //System.out.println("La codificacion del PDF es: " + encoding);
                        arrayEncoding.add(encoding);
                        

                        String lengua = languageDetection(text.toString());
                        //System.out.println("PDF escrito en : " + lengua);
                        idiomas.add(lengua);
                        for(int d = 0;d < linkHandler.getLinks().size();d++ ){
                            links.add(linkHandler.getLinks().get(d).getText());
                           //System.out.println("Link " + d+ " del HTML: " + linkHandler.getLinks().get(d).getText());
                        }
                        //Conteo(text,f);
                        textos.add(text);
                        break;

                    case "text/html":
                        //System.out.println("Tipo: " + type);
                        tipos.add(type);
                        // Creamos los 3 handler para almacenar lo correspondiente
                        BodyContentHandler textHandlerHtml = new BodyContentHandler();
                        LinkContentHandler linkHandlerHtml = new LinkContentHandler();
                        ToHTMLContentHandler toHtmlContentHandler = new ToHTMLContentHandler();
                        // El teeContentHandler es un contenedor de handler
                        TeeContentHandler teeHandlerHtml = new TeeContentHandler(linkHandlerHtml,textHandlerHtml,toHtmlContentHandler);
                        // Creamos instancia de metadata
                        Metadata metadataHtml = new Metadata();
                        // Creamos instancia de contexto
                        ParseContext pcontextHtml = new ParseContext();
                        // Convertimos a InputStream para poder pasarlo como parámetro
                        InputStream ficheroInputHtml = new FileInputStream(f);
                        HtmlParser parserHtml = new HtmlParser();
                        //parserHtml.parse(ficheroInputHtml, teeHandlerHtml, metadataHtml, pcontextHtml);
                        parserHtml.parse(ficheroInputHtml, teeHandlerHtml, metadataHtml, pcontextHtml);
                        String encodingHtml = guessEncoding(ficheroInputHtml);
                        //System.out.println("La codificacion del HTML es: " + encodingHtml);
                        arrayEncoding.add(encodingHtml);
                        // OPCION POR SI FALLA EN UN FUTURO. EL TEXT EN VEZ DE EL TEXThANDLER
                        //String text = tika.parseToString(f); 
                        //String lengua = languageDetection(text);
                        String lenguaHtml = languageDetection(teeHandlerHtml.toString());
                        //System.out.println("HTML escrito en : " + lenguaHtml);
                        idiomas.add(lenguaHtml);
                        for(int j = 0;j < linkHandlerHtml.getLinks().size();j++ ){
                            links.add(linkHandlerHtml.getLinks().get(j).getText());
                            //System.out.println("Link " + j + " del HTML: " + linkHandlerHtml.getLinks().get(j).getText());
                        }
                        textos.add(text);
                        //Conteo(text.toString(),f);

                        break;

                    case "text/plain":
                        //System.out.println("Tipo: " + type);
                        tipos.add(type);
                        InputStream ficheroInputTXT = new FileInputStream(f);
                        // Creamos los 3 handler para almacenar lo correspondiente
                        //BodyContentHandler textHandlerTXT = new BodyContentHandler();
                        LinkContentHandler linkHandlerTXT = new LinkContentHandler();
                        // El teeContentHandler es un contenedor de handler
                        //TeeContentHandler teeHandlerTXT = new TeeContentHandler(linkHandlerTXT);
                        // Creamos instancia de metadata
                        Metadata metadataTXT = new Metadata();
                        // Creamos instancia de contexto
                        ParseContext pcontextTXT = new ParseContext();
                        // Creamos instancia del parseador de TXT
                        TXTParser txtparser = new TXTParser();
                        // Analizamos el fichero y guardamos lo que corresponde en cada handler
                        txtparser.parse(ficheroInputTXT,linkHandlerTXT,metadataTXT,pcontextTXT);
                        //System.out.println("Contenido: " + textHandler.toString());
                        // Llamamos a la función encargada de sacar el tipo de coding
                        String encodingTXT = guessEncoding(ficheroInputTXT);
                        //System.out.println("La codificacion del TXT: " + encodingTXT);

                        // Añadimos al array de encoding
                        arrayEncoding.add(encodingTXT);

                        String textTXT = tika.parseToString(f); 
                        String lenguaTXT = languageDetection(textTXT.toString());
                        //System.out.println("TXT escrito en : " + lenguaTXT);
                        idiomas.add(lenguaTXT);
                        for(int d = 0;d < linkHandlerTXT.getLinks().size();d++ ){
                            links.add(linkHandlerTXT.getLinks().get(d).getText());
                            //System.out.println("Link " + d+ " del HTML: " + linkHandlerTXT.getLinks().get(d).getText());
                        }
                        textos.add(text);
                        //Conteo(text,f);
                        break;
                }

                switch(opcion){

                    // Nombre, tipo, encoding e idioma
                    case "-d":
                        for (int g=0; g< nombres.size(); g++){
                            System.out.println("Nombre: " + nombres.get(g).toString());
                            System.out.println("Idioma: " + idiomas.get(g).toString());
                            System.out.println("Tipo: " + tipos.get(g).toString());
                            System.out.println("Encoding: " + arrayEncoding.get(g).toString());
                            System.out.println("-----------------------------");
                        }
                        break;
                    // Links
                    case "-l":
                        for (int l=0; l<links.size(); l++){
                           System.out.println("Links: " + links.get(l).toString());
                        }
                        break;
                    // Genera un fichero CSV por cada archivo
                    case "-t":
                        Conteo(text,f);
                        System.out.println("CSV de " + f.getName()+ " generado satisfactoriamente");
                        break;
                }
            }
        }
    }

    // Bibliografia --> http://useof.org/java-open-source/org.apache.tika.language.detect.LanguageDetector
    // https://tika.apache.org/1.16/api/org/apache/tika/language/detect/LanguageDetector.html

    public static String languageDetection(String text) throws IOException {
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        return detector.detect(text).getLanguage();
        //return result.getLanguage();
    }

    // Bibliografia https://www.tabnine.com/code/java/methods/org.apache.tika.parser.txt.CharsetDetector/%3Cinit%3E
    /*
    Guesses the data encoding.
    Specified by:
    guessEncoding in interface EncodingDetector
    Parameters:
    input - the input stream containing the data.
    Returns:
    a string compliant to IANA Charset Specification.
    Throws:
    IOException - if there is an error whilst guessing the encoding
    */

    public static String guessEncoding(InputStream is) throws IOException {
        //Creamos un objeto de la clase. Proporciona un método para detectar el juego de caracteres
        CharsetDetector charsetDetector = new CharsetDetector();
        //Establezca los datos de texto de entrada (bytes) cuyo juego de caracteres se va a detectar
        charsetDetector.setText(is instanceof BufferedInputStream ? is : new BufferedInputStream(is));
        // Ponemos el filtraod a true
        charsetDetector.enableInputFilter(true);
        // Detectamos y lo guardamos en un CharSetMatch
        //Esta clase representa un conjunto de caracteres que ha sido identificado por un CharsetDetector como una posible codificación
        return charsetDetector.detect().getName();
        //return cm.getName();
    }

    // Función para contar el número de palabras que aparecen
    public static void Conteo(String input,File f) throws IOException {
        String[] palabras = input.split(" ");

        // Creamos una tabla hash 
        Map<String, Integer> tablaHash = new HashMap<String, Integer>();
        for(String palabra : palabras){
            if(tablaHash.containsKey(palabra)){
                int frequency = tablaHash.get(palabra);
                tablaHash.put(palabra, frequency + 1);
            }else{
                tablaHash.put(palabra, 1);
            }
        }

        List<Entry<String, Integer>> list = new ArrayList<>(tablaHash.entrySet());
        PrintWriter out = new PrintWriter(new File((f.getName()+".csv")));
        list.sort(Entry.comparingByValue());
        for(int a=list.size()-1;a>=0;a--){
            out.println(list.get(a).getKey()+ ";" + list.get(a).getValue());
        }
        out.close();
    }

}

