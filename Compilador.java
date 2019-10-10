import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//Espacio al final? Comnetario al final? Comentario a medias al final?


public  class Compilador {
	static char car;
	static MatrizTransiciones MT_AFD = new MatrizTransiciones();
	static TS TablaSimbolos = new TS();
	static BufferedReader br;
	static BufferedWriter bw;
	//Para las acciones semanticas
	static int num = 0;
	static String lex = "";
	
	public static class TS {
		private List<TSElem> tabla;
		public TS(){
			tabla = new ArrayList<TSElem>();
		}

		//Quitar el parametro, ya que siempre opera con lex...
		public int buscaTS(){
			int posTS=0;
			while (posTS<tabla.size() && !tabla.get(posTS).getLexema().equals(lex)){
				posTS++;
			}
			return posTS == tabla.size() ? -1 : posTS;
		}

		public int anadeTS(){
			tabla.add(new TSElem(lex));
			return tabla.size() - 1;
		}
	}
	
	//Filas de la TS: lexema, tipo, despl., NArgs, tipoArgs, tipoDevuelto
	//El ALex. solo mete el lexema
	public static class TSElem {
		private String lexema;
		private String tipo;
		private int desplazamiento;
		private int NArgs;
		private String[] tipoArgs;
		private String tipoDevuelto;
		
		public TSElem(String lexema) {
			this.lexema = lexema;
		}
		
		public String getLexema() {
			return lexema;
		}

		public void setLexema(String lexema) {
			this.lexema = lexema;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public int getDesplazamiento() {
			return desplazamiento;
		}

		public void setDesplazamiento(int desplazamiento) {
			this.desplazamiento = desplazamiento;
		}

		public int getNArgs() {
			return NArgs;
		}

		public void setNArgs(int nArgs) {
			NArgs = nArgs;
		}

		public String[] getTipoArgs() {
			return tipoArgs;
		}

		public void setTipoArgs(String[] tipoArgs) {
			this.tipoArgs = tipoArgs;
		}

		public String getTipoDevuelto() {
			return tipoDevuelto;
		}

		public void setTipoDevuelto(String tipoDevuelto) {
			this.tipoDevuelto = tipoDevuelto;
		}
	}
	
	//Contenido de la MatrizTransiciones
	private static class ParEstadoAccion {
		private int estado;
		private String accion;
		
		public ParEstadoAccion (int estado, String accion) {
			this.estado = estado;
			this.accion = accion;
		}
		
		public int getEstado() {
			return estado;
		}
		
		public String getAccion() {
			return accion;
		}
	}
	
	public static class MatrizTransiciones{
		
		/*
		 * Filas: 0 - 21 (a partir del 8 son estados finales)
		 * Columnas: |  l  d  '  /  _  c  *  del  ;  {  }  (  )  +  <  !   =  ,  
		 *   (estan en orden, ademas hay una funcion para dar el indice de cada uno)
		 * 
		 * Acciones semanticas: (en todas se lee salvo en "error", y en G2 y G3 por ser o.c.)
		 * "lee": solo se lee
		 * "error": transicion no prevista  
		 * "A": inicializa el valor del numero
		 * "B": calcula el nuevo valor del numero
		 * "C": concat
		 * "Gx": generar token correspondiente
		 * 
		 * Errores: -1 como estado de las transiciones no previstas (que tienen accion semantica "error")
		 */ 
		private ParEstadoAccion[][] matriz;
		public MatrizTransiciones() {
			this.matriz = new ParEstadoAccion[22][19]; 
			
			//del -> ' ' , l -> 'a' , d -> '1' , c -> '.'
			matriz[0][char2int('|')] = new ParEstadoAccion(1, "lee");
			matriz[0][char2int('a')] = new ParEstadoAccion(2, "C");
			matriz[0][char2int('1')] = new ParEstadoAccion(3, "A");
			matriz[0][char2int('\'')] = new ParEstadoAccion(4, "lee");
			matriz[0][char2int('/')] = new ParEstadoAccion(5, "lee");
			matriz[0][char2int('_')] = new ParEstadoAccion(-1, "error");
			matriz[0][char2int('.')] = new ParEstadoAccion(-1, "error");
			matriz[0][char2int('*')] = new ParEstadoAccion(-1, "error");
			matriz[0][char2int(' ')] = new ParEstadoAccion(0, "lee");
			//Finales 12-21, Acciones G5-G14
			for (int i = 9; i < 19; i++) matriz[0][i] = new ParEstadoAccion(i+3, "G"+Integer.toString(i-4));
			
			for (int i = 0; i < 19; i++) matriz[1][i] = new ParEstadoAccion(-1, "error");
			matriz[1][char2int('=')] = new ParEstadoAccion(8, "G1");
			
			//Todas las que no son l, d, _, son o.c.
			for (int i = 0; i < 19; i++) matriz[2][i] = new ParEstadoAccion(9, "G2");
			matriz[2][char2int('a')] = new ParEstadoAccion(2, "C");
			matriz[2][char2int('1')] = new ParEstadoAccion(2, "C");
			matriz[2][char2int('_')] = new ParEstadoAccion(2, "C");
			
			//Todas las que no son d son o.c.
			for (int i = 0; i < 19; i++) matriz[3][i] = new ParEstadoAccion(10, "G3");
			matriz[3][char2int('1')] = new ParEstadoAccion(3, "B");
			
			for (int i = 0; i < 19; i++) matriz[4][i] = new ParEstadoAccion(4, "C");
			matriz[4][char2int('\'')] = new ParEstadoAccion(11, "G4");
			
			for (int i = 0; i < 19; i++) matriz[5][i] = new ParEstadoAccion(-1, "error");
			matriz[5][char2int('*')] = new ParEstadoAccion(6, "lee");
			
			//Cualquier cosa que no sea * lo lee sin mas
			for (int i = 0; i < 19; i++) matriz[6][i] = new ParEstadoAccion(6, "lee");
			matriz[6][char2int('*')] = new ParEstadoAccion(7, "lee");
			
			for (int i = 0; i < 19; i++) matriz[7][i] = new ParEstadoAccion(6, "lee");
			matriz[7][char2int('/')] = new ParEstadoAccion(0, "lee");
			matriz[7][char2int('*')] = new ParEstadoAccion(7, "lee");
		
		}
		
		public static int char2int(char c) {
			if(( (int)c >= 65 && (int)c <= 90 ) || ( (int)c >= 97 && (int)c <= 122 ))
				return 1;
			else{
				switch (c) {
					case '|':
						return 0;
					case '0':  
					case '1':  
					case '2':  
					case '3':  
					case '4':  
					case '5':  
					case '6':
					case '7':  
					case '8':  
					case '9':  
						return 2;
					case '\'':  
						return 3;
					case '/':
						return 4;
					case '_':  
						return 5;
					case '*':  
						return 7;
					case ' ':  
					case '\t':
					case '\n': 
					case '\r': //cr
						return 8;
					case ';':  
						return 9;
					case '{':  
						return 10;
					case '}':  
						return 11;
					case '(':  
						return 12;
					case ')':  
						return 13;
					case '+':  
						return 14;
					case '<':  
						return 15;
					case '!':   
						return 16;
					case '=':  
						return 17;
					case ',':  
						return 18;
					//Caracteres (c)
					default:
						return 6;
				}
			}
		}
		
		//Devuelve el estado correspondiente a la posicion (estado, car)
		public int estado (int estado, char car){
			//Si esta leyendo un comentario y se encuentra el final o una cadena (sin cerrar)
			if (car == '\0' && (estado == 6 || estado == 7 || estado == 4)) return -1;
			//o si estaba leyendo espacios al final
			if (car == '\0' && estado == 0) error(0);
			return matriz[estado][char2int(car)].getEstado();
		}

		//Devuelve la accion correspondiente a la posicion (estado, car)
		public String accion (int estado, char car){
			//if (car == '\0' && (estado == 6 || estado == 7) || estado == 4) return "error";
			//if (car == '\0' && estado == 0) error(0);
			return matriz[estado][char2int(car)].getAccion();
		}
	}
	
	private static class Token<E>{
		private String codToken;
		private E atributo;
		
		public Token(String codToken, E atributo){
			this.codToken=codToken;
			this.atributo=atributo;
		}
		
		//Contructor sin atributo
		public Token(String codToken){
			this.codToken=codToken;
			this.atributo=null;
		}

		public String getCodToken() {
			return codToken;
		}

		public E getAtributo() {
			return atributo;
		}

		public void setAtributo(E atributo) {
			this.atributo = atributo;
		}
		
	}
	
	public static Token<?> ALex (){
		//Resetear los valores
		int estado = 0;
		lex = "";
		num = 0;
		Token<?> token = null;
		while (estado < 8){
			//System.out.println("est:" + estado + " car: " + car);
			String accion = MT_AFD.accion(estado, car);
			estado = MT_AFD.estado(estado, car);
			if(estado == -1){
				//Manda mensaje de error, escribe la tabla de simbolos y sale del programa
				error(1);
			}else{
				
				switch(accion){
					case "lee":
						lee();
						break;
					case "A":
						A();
						break;
					case "B":
						B();
						break;
					case "C":
						C();
						break;
					default:
						switch(Integer.parseInt(accion.substring(1))){
						case 1:
							token=G1();
							break;
						case 2:
							token=G2();
							break;
						case 3:
							token=G3();
							break;
						case 4:
							token=G4();
							break;
						case 5:
							token=G5();
							break;
						case 6:
							token=G6();
							break;
						case 7:
							token=G7();
							break;
						case 8:
							token=G8();
							break;
						case 9:
							token=G9();
							break;
						case 10:
							token=G10();
							break;
						case 11:
							token=G11();
							break;
						case 12:
							token=G12();
							break;
						case 13:
							token=G13();
							break;
						case 14:
							token=G14();
					}
					
				}
			}
		} 
		escribirToken(token);
		return token;
	}
	
	
	//Acciones semanticas
	public static void lee(){
		int caracterInt=-1;
		try {
			if((caracterInt=br.read())!=-1){
				car = (char) caracterInt;
				//Chapuza para que reconozca el final del fichero 
			} else {
				car = '\0';
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void error(int codError) {
		switch (codError) {
		case 0:
			break;
		case 1:
			System.err.println("Error: transicion no prevista");
			break;
		case 2:
			System.err.println("Error: numero fuera de rango");
			break;
		}
		escribirTablaSimbolos();
		//Sin esto, no vuelca nada en el fichero en caso de error
		try {
			br.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(1);
	}
	
	
	private static void A(){
		//Ojo al orden
		num = Character.getNumericValue(car);
		lee();
	}
	
	private static void B(){
		//Ojo al orden
		num = num * 10 + Character.getNumericValue(car);
		lee();
	}
	
	
	private static void C(){
		//Ojo al orden
		lex += car;
		lee();
	}
	
	private static Token<Integer> G1(){
		lee();
		return new Token<Integer>("ASIGOR");
		
	}
	
	//palRes es un conjunto de Strings
	private static Token<Integer> G2(){
		switch(lex){
			case "var":
				return new Token<Integer>("DEC");
			case "int":
				return new Token<Integer>("TipoVarENT");
			case "boolean":
				return new Token<Integer>("TipoVarLOG");
			case "string":
				return new Token<Integer>("TipoVarCAD");
			case "print":
				return new Token<Integer>("Print");
			case "input":
				return new Token<Integer>("Input");
			case "return":
				return new Token<Integer>("Return");
			case "function":
				return new Token<Integer>("DECFunc");
			case "if":
				return new Token<Integer>("IF");
			case "else":
				return new Token<Integer>("ELSE");
			default:
				int p;
				if ((p = TablaSimbolos.buscaTS()) == -1) p = TablaSimbolos.anadeTS();
				return new Token<Integer>("ID", p);
		}
	}
	
	private static Token<Integer> G3(){
		if (num >= Math.pow(2, 15)) error(2); //return new Token<Integer>("ENT_ERROR", -1);
		return new Token<Integer>("ENT", num);
	}
	
	private static Token<String> G4(){
		lee();
		return new Token<String>("CAD", lex + "\0");
	}
		
	private static Token<Integer> G5(){
		lee();
		return new Token<Integer>("PuntoComa");
	}
	
	private static Token<Integer> G6() {
		lee();
		return new Token<Integer>("CorcheteAbrir");
	}
	private static Token<Integer> G7() {
		lee();
		return new Token<Integer>("CorcheteCerrar");
	}
	private static Token<Integer> G8() {
		lee();
		return new Token<Integer>("ParentesisAbrir");
	}
	private static Token<Integer> G9() {
		lee();
		return new Token<Integer>("ParentesisCerrar");
	}
	private static Token<Integer> G10() {
		lee();
		return new Token<Integer>("SUMA");
	}
	private static Token<Integer> G11() {
		lee();
		return new Token<Integer>("MENOR");
	}
	private static Token<Integer> G12() {
		lee();
		return new Token<Integer>("NOT");
	}
	private static Token<Integer> G13() {
		lee();
		return new Token<Integer>("ASIG");
	}
	private static Token<Integer> G14() {
		lee();
		return new Token<Integer>("Coma");
	}
	
	
	//Escribe el token en el archivo
	private static <E> void escribirToken(Token<E> token){
	    String output="";
	    String codToken=token.getCodToken();
	    E atributo=token.getAtributo();
	    if(atributo==null) 
	    	output=" <"+ codToken + ", > \n";
	    else
	    	output=" <"+ codToken + ", "+atributo+"> \n";
	
	      try {
			bw.write(output);
	      } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	      }
	}
	
	private static void escribirTablaSimbolos(){
		//TODO Escribir la tabla de simbolos en otro fichero con el formato requerido
	}
	
	
	public static void main(String []args){
		//File file = new File(args[0]);
		File file = new File("path");
		br = null;
		bw = null;
		try {
			br = new BufferedReader(new FileReader(file));
			bw = new BufferedWriter(new FileWriter("tokens.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lee();
		while(car!='\0') ALex();
		
		escribirTablaSimbolos();
		
		try {
			br.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}