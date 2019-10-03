import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Compilador {
	char car;
	final  MatrizTransiciones MT_AFD = new MatrizTransiciones();
	static BufferedReader br;
	
	public class MatrizTransiciones{
		public char accion (int estado, char car){
			return 0;
		}
		
		public int estado (int estado, char car){
			return -1;
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
