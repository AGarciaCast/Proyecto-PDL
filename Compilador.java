import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Compilador {
	char car;
	final MatrizTransiciones MT_AFD = new MatrizTransiciones();
	static BufferedReader br;
	
	//Contenido de la MatrizTransiciones
	public class ParEstadoAccion {
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
	
	public class MatrizTransiciones{
		
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
			
			//del -> ' ' , l -> 'l' , d -> '1' , c -> '.'
			matriz[0][char2int('|')] = new ParEstadoAccion(1, "lee");
			matriz[0][char2int('l')] = new ParEstadoAccion(2, "C");
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
			matriz[2][char2int('l')] = new ParEstadoAccion(2, "C");
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
		
		public int char2int(char c) {
			switch (c) {
			case '|':
				return 0;
			//TODO: Poner cada letra...
			case 'l':  
				return 1;
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
			/*En el default
			case 'c':  
				return 6;*/
			case '*':  
				return 7;
			//Poner todos los delimitadores?
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
		
		//Devuelve el estado correspondiente a la posicion (estado, car)
		public int estado (int estado, char car){
			return matriz[estado][char2int(car)].getEstado();
		}
				
		//Devuelve la accion correspondiente a la posicion (estado, car)
		public String accion (int estado, char car){
			return matriz[estado][char2int(car)].getAccion();
		}
	}
	
	public class Token<E>{
		private int codToken;
		private E atributo;
		
		public Token(int codToken, E atributo){
			this.codToken=codToken;
			this.atributo=atributo;
		}

		public int getCodToken() {
			return codToken;
		}

		public E getAtributo() {
			return atributo;
		}

		public void setAtributo(E atributo) {
			this.atributo = atributo;
		}
		
	}
	
	public void ALex (){
		int estado = 0;
		while (estado < 8){
			char accion = MT_AFD.accion(estado, car);
			estado = MT_AFD.estado(estado, car);
			if(estado == -1){
				//error
			}else{
				switch(accion){
					
				}
			}
			
			
		}
	}
	
	public void lee(){
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
	
	public static void main(String []args){
		File file = new File("Path");
		br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int caracterInt=-1;
		try {
			while((caracterInt=br.read())!=-1){
				car = (char) caracterInt;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
