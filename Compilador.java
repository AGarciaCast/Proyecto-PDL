import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public  class Compilador {
	static char car;
	static MatrizTransiciones MT_AFD = new MatrizTransiciones();
	static BufferedReader br;
	//Para las acciones semanticas
	static int num = 0;
	static String lex = "";
	
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
			
			for (int i = 0; i < 19; i++) matriz[4][i] = new ParEstadoAccion(-1, "error");
			matriz[4][char2int('\'')] = new ParEstadoAccion(11, "G4");
			matriz[4][char2int('/')] = new ParEstadoAccion(4, "C");
			matriz[4][char2int('.')] = new ParEstadoAccion(4, "C");
			matriz[4][char2int('*')] = new ParEstadoAccion(4, "C");
			
			for (int i = 0; i < 19; i++) matriz[5][i] = new ParEstadoAccion(-1, "error");
			matriz[5][char2int('*')] = new ParEstadoAccion(6, "lee");
			
			for (int i = 0; i < 19; i++) matriz[6][i] = new ParEstadoAccion(-1, "error");
			matriz[6][char2int('.')] = new ParEstadoAccion(6, "lee");
			matriz[6][char2int('/')] = new ParEstadoAccion(6, "lee");
			matriz[6][char2int('*')] = new ParEstadoAccion(7, "lee");
			
			for (int i = 0; i < 19; i++) matriz[7][i] = new ParEstadoAccion(-1, "error");
			matriz[7][char2int('/')] = new ParEstadoAccion(0, "lee");
			matriz[7][char2int('.')] = new ParEstadoAccion(6, "lee");
			matriz[7][char2int('*')] = new ParEstadoAccion(7, "lee");
		
		}
		
		public static int char2int(char c) {
			if(((int)c >= 65 && (int)c<=90 )||((int)c >= 97 && (int)c<=122))
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
			return matriz[estado][char2int(car)].getEstado();
		}
				
		//Devuelve la accion correspondiente a la posicion (estado, car)
		public String accion (int estado, char car){
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
		
		//No creo que hagan falta estos metodos
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
	
	public static void  ALex (){
		int estado = 0;
		while (estado < 8){
			String accion = MT_AFD.accion(estado, car);
			estado = MT_AFD.estado(estado, car);
			if(estado == -1){
				//error
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
							G1();
							break;
						case 2:
							G2();
							break;
						case 3:
							G3();
							break;
						case 4:
							G4();
							break;
						case 5:
							G5();
							break;
						case 6:
							G6();
							break;
						case 7:
							G7();
							break;
						case 8:
							G8();
							break;
						case 9:
							G9();
							break;
						case 10:
							G10();
							break;
						case 11:
							G11();
							break;
						case 12:
							G12();
							break;
						case 13:
							G13();
							break;
						case 14:
							G14();
					}
					
				}
			}
		}
	}
	
	
	//Acciones semanticas
	public static void lee(){
		int caracterInt=-1;
		try {
			if((caracterInt=br.read())!=-1){
				car = (char) caracterInt;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void error() {
		System.out.println("Error en transicion no prevista");
	}
	
	private static void A(){
		lee();
		num = Character.getNumericValue(car);
	}
	
	private static void B(){
		lee();
		num = num*10 + Character.getNumericValue(car);
	}
	
	private static void C(){
		lee();
		lex += car;
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
				if ((p = buscaTS()) == -1) p = anadeTS();
				return new Token<Integer>("ID", p);
		}
	}
	
	private static Token<Integer> G3(){
		if (num >= Math.pow(2, 15)) return new Token<Integer>("", -1); //ERROR
		return new Token<Integer>("ENT", num);
	}
	
	private static Token<String> G4(){
		lee();
		return new Token<String>("CAD", lex);
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
	
	//Busca lex en TS
	public static int buscaTS() {
		//TODO
		return 0;
	}
	
	//Introduce lex en TS
	public static int anadeTS() {
		//TODO
		return 0;
	}
	
	
	public static void main(String []args){
		File file = new File(args[0]);
		br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lee();
		while(car!='\0')
			ALex();
		
	}
	
}