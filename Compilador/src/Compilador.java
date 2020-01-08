import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class Compilador {
	static char car;
	static MatrizTransiciones MT_AFD = new MatrizTransiciones();
	//ID de cada tabla, al crear una nueva se le asigna el valor y se aumenta para tablas posteriores
	static int numTS = 1;
	//Solo dos Tablas porque no hay anidamiento
	//De momento solo trabajo con la Global
	static TS TablaSimbolosGlobal; 
	static TS TablaSimbolosLocal;
	static TS TablaSimbolosActual = null;
	//Escribir en ficheros
	static BufferedReader br;
	static BufferedWriter bw;
	static BufferedWriter bwTS;
	static BufferedWriter bwSt;
	//Para las acciones semanticas
	static int num = 0;
	static int cont = 0;
	static String lex = "";
	//Para llevar la cuenta de la linea
	static int linea = 1;
	//Tabla ACCION GOTO
	static TablaDecisionLR TDecLR = new TablaDecisionLR();
	//Pila de Trabajo
	static Stack<Integer> pilaSt = new Stack<Integer>();
	//Gramatica
	static Gramatica gramatica = new Gramatica();
	//Tipo Error
	static final int ERR_LEX = 0;
	static final int ERR_ST = 1;
	static final int ERR_SE = 2;
	//Pila Semantica
	static Stack<ElemSem> pilaSem = new Stack<ElemSem>();
	static int desplG;
	static int desplL;
	static boolean zona_decl = true;

	public static class ElemSem {
		private List<String> tipoLista=null;
		private String tipo;
		private int tamano;
		private String posi;
		private int num;
		private String cadena;
		private String tipoRet;
		
		public ElemSem() {
			
		}
		
		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public String getCadena() {
			return cadena;
		}

		public void setCadena(String cadena) {
			this.cadena = cadena;
		}

		public String getTipoRet() {
			return tipoRet;
		}

		public void setTipoRet(String tipoRet) {
			this.tipoRet = tipoRet;
		}

		public List<String> getTipoLista() {
			return tipoLista;
		}
		
		public String getTipo() {
			return tipo;
		}
		
		public int getTamano() {
			return tamano;
		}
		
		public String getPosi() {
			return posi;
		}
		
		public void setTipo(String tipo) {
			this.tipo = tipo;
		}
		
		public void setTipoLista(String tipo) {
			if (this.tipoLista==null) this.tipoLista=new ArrayList<String>();
			this.tipoLista.add(tipo);
		}
		
		public void setTamano(int tamano) {
			this.tamano = tamano;
		}
		
		public void setPosi(String posi) {
			this.posi = posi;
		}

	}
	
	public static class TS {

		private String id; 
		private int num;
		private List<TSElem> tabla;

		public TS(String id){
			this.num=numTS++;
			this.id = id;
			tabla = new ArrayList<TSElem>();
		}
		
		
		public String buscaTS(String lexema){
			int posTS=0;
			while (posTS<tabla.size() && !tabla.get(posTS).getLexema().equals(lexema)){
				posTS++;
			}
			return posTS == tabla.size() ? null : id+posTS;
		}
		
		public String anadeTS(String lex){
			tabla.add(new TSElem(lex));
			return id+(tabla.size() - 1); 
		}

		public List<TSElem> getTabla() {
			return tabla;
		}
		
		public int getNum() {
			return num;
		}
		
		public String getId() {
			return id;
		}
	}
	
	
	public static String buscaTS(String lexema){
		String resultado = null;
		if(TablaSimbolosActual==TablaSimbolosLocal){
			int posTS=0;
			List<TSElem> tabla = TablaSimbolosLocal.getTabla();
			while (posTS<tabla.size() && !tabla.get(posTS).getLexema().equals(lexema)){
				posTS++;
			}
			resultado= posTS == tabla.size() ? null : TablaSimbolosLocal.getId()+posTS;
		}
		if(resultado == null){
			int posTS=0;
			List<TSElem> tabla = TablaSimbolosGlobal.getTabla();
			while (posTS<tabla.size() && !tabla.get(posTS).getLexema().equals(lexema)){
				posTS++;
			}
			resultado= posTS == tabla.size() ? null : TablaSimbolosGlobal.getId()+posTS;
		}
		return resultado;
	}

	public static String buscaTipoTS(String string){
		//TODO
		return null;
	}
	
	public static String[] buscaTipoTSLista(String lexema){
		//TODO
		return null;
		
	}
	
	public static String porFunc(String tipo, String tipo2) {
		//TODO
		return null;
	}
	
	//Filas de la TS: lexema, tipo, despl., NArgs, tipoArgs, tipoDevuelto
	//El ALex. solo mete el lexema
	public static class TSElem {
		private String lexema;
		private String tipo;
		private int desplazamiento;
		private int NArgs;
		private String[] tipoArgs;
		private String[] modoArgs;
		private String tipoDevuelto;

		public TSElem(String lexema) {
			this.lexema = lexema;
			//Para no confundir no estar inicializado con tener valor 0
			this.desplazamiento = -1;
			this.NArgs = -1;
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
			//Crea los arrays en funcion del numero de argumentos
			tipoArgs = new String[NArgs];
			modoArgs = new String[NArgs];
		}

		public String getTipoArgs(int index) {
			return tipoArgs[index];
		}

		public void setTipoArgs(String tipo, int index) {
			this.tipoArgs[index] = tipo;
		}

		public String getModoArgs(int index) {
			return modoArgs[index];
		}

		public void setModoArgs(String modo, int index) {
			this.modoArgs[index] = modo;
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
			matriz[0][char2int('\'')] = new ParEstadoAccion(4, "D");
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

			for (int i = 0; i < 19; i++) matriz[4][i] = new ParEstadoAccion(4, "E");
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
				case '\r': //cr
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
			//Si esta leyendo un comentario y se encuentra el final o una cadena (sin cerrar)
			if (car == '\0' && (estado == 6 || estado == 7 || estado == 4)) return -1;
			//o si estaba leyendo espacios al final
			if (car == '\0' && estado == 0) return -2; //error(0);
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
			this.codToken = codToken;
			this.atributo = atributo;
		}

		//Contructor sin atributo
		public Token(String codToken){
			this.codToken = codToken;
			this.atributo = null;
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
			if (estado == -2) return new Token<Integer>("$");
            linea = car == '\n' ? linea + 1 : linea;
			//System.out.println("est:" + estado + " car: " + car);
			String accion = MT_AFD.accion(estado, car);
			estado = MT_AFD.estado(estado, car);
			if(estado == -1){
				//Manda mensaje de error, escribe la tabla de simbolos y sale del programa
				gestorErrores(ERR_LEX, 1);
			} else {

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
				case "D":
					D();
					break;
				case "E":
					E();
					break;
				case "G1":
					token = G1();
					break;
				case "G2":
					token = G2();
					break;
				case "G3":
					token = G3();
					break;
				case "G4":
					token = G4();
					break;
				case "G5":
					token = G5();
					break;
				case "G6":
					token = G6();
					break;
				case "G7":
					token = G7();
					break;
				case "G8":
					token = G8();
					break;
				case "G9":
					token = G9();
					break;
				case "G10":
					token = G10();
					break;
				case "G11":
					token = G11();
					break;
				case "G12":
					token = G12();
					break;
				case "G13":
					token = G13();
					break;
				case "G14":
					token = G14();
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

	/*public static void error(int codError) {
		switch (codError) {
		case 0:
			break;
		case 1:
			System.err.println("Error: transicion no prevista." + "  Linea: " + linea);
			break;
		case 2:
			System.err.println("Error: numero fuera de rango." + "  Linea: " + linea);
			break;
		}
		escribirTablaSimbolos(TablaSimbolosGlobal);
		//Sin esto, no vuelca nada en el fichero en caso de error
		try {
			br.close();
			bw.close();
			bwTS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(1);
	}*/


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

	private static void D(){
		//Ojo al orden
		cont = 0;
		lee();
	}
	
	private static void E(){
		//Ojo al orden
		cont++;
		lex += car;
		lee();
	}
	
	private static Token<Integer> G1(){
		lee();
		return new Token<Integer>("ASIGOR");

	}

	//palRes es un conjunto de Strings
	private static Token<String> G2(){
		switch(lex){
		case "var":
			return new Token<String>("DEC");
		case "int":
			return new Token<String>("TipoVarENT");
		case "boolean":
			return new Token<String>("TipoVarLOG");
		case "string":
			return new Token<String>("TipoVarCAD");
		case "print":
			return new Token<String>("Print");
		case "input":
			return new Token<String>("Input");
		case "return":
			return new Token<String>("Return");
		case "function":
			return new Token<String>("DECFunc");
		case "if":
			return new Token<String>("IF");
		case "else":
			return new Token<String>("ELSE");
		default:
			int p;
			String posi=null;
			if (zona_decl){
				if(TablaSimbolosActual.buscaTS(lex)!=null)
					gestorErrores(ERR_LEX,4);
				else
					posi=TablaSimbolosActual.anadeTS(lex);
			}else{
				posi = buscaTS(lex);
				if (posi == null){
					posi = TablaSimbolosGlobal.anadeTS(lex);
					p = Integer.parseInt(posi.substring(1));
					TablaSimbolosActual.getTabla().get(p).setTipo("entero");
					TablaSimbolosActual.getTabla().get(p).setDesplazamiento(desplG);
					desplG ++;
				}
			}
			
			return new Token<String>("ID", posi);
		}
	}

	private static Token<Integer> G3(){
		if (num >= Math.pow(2, 15)) gestorErrores(ERR_LEX, 2);
		return new Token<Integer>("ENT", num);
	}

	private static Token<String> G4(){
		lee();
		if (cont > 64) gestorErrores(ERR_LEX, 3);
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


	//Escribe el token en el archivo
	private static <E> void escribirToken(Token<E> token){
		String output = "";
		String codToken = token.getCodToken();
		E atributo = token.getAtributo();
		if (atributo == null) {
			output = " <"+ codToken + ", > \n";
		}
		//Anade comillas
		else if (codToken.equals("CAD")) {
			output = " <" + codToken + ", \"" + atributo + "\"> \n";
		} else {
			output = " <" + codToken + ", " + atributo + "> \n";
		}
		try {
			bw.write(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void liberaTS(TS tablaSimbolos){
		//Necesito resetear el writer para escribir cada tabla en un fichero
		String file = "TS" + tablaSimbolos.getId() + ".txt";
		try {
			bwTS = new BufferedWriter(new FileWriter(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String cabecera = "Tabla Simbolos #" + tablaSimbolos.getNum() + ":\n";
		String output = cabecera + "\n";

		for (int i = 0; i < tablaSimbolos.getTabla().size(); i++) {
			String lineaLexema = " * LEXEMA: \'" + tablaSimbolos.getTabla().get(i).getLexema() + "\'\n";

			String atributos = "";

			if (tablaSimbolos.getTabla().get(i).getTipo() != null) 
				atributos += "  + Tipo: \'" + tablaSimbolos.getTabla().get(i).getTipo() +"\'\n";

			if (tablaSimbolos.getTabla().get(i).getDesplazamiento() != -1)
				atributos += "  + Despl: " + tablaSimbolos.getTabla().get(i).getDesplazamiento() +"\n";

			if (tablaSimbolos.getTabla().get(i).getNArgs() != -1)
				atributos += "  + numParam: " + tablaSimbolos.getTabla().get(i).getNArgs() +"\n";

			for (int j = 0; j < tablaSimbolos.getTabla().get(i).getNArgs(); j++) {
				if (tablaSimbolos.getTabla().get(i).getTipoArgs(j) != null)
					atributos += "  + TipoParam" + j + ": \'" + tablaSimbolos.getTabla().get(i).getTipoArgs(j) +"\'\n";
				if (tablaSimbolos.getTabla().get(i).getModoArgs(j) != null)
					atributos += "  + ModoParam" + j + ": \'" + tablaSimbolos.getTabla().get(i).getModoArgs(j) +"\'\n";
			}
			if (tablaSimbolos.getTabla().get(i).getTipoDevuelto() != null)
				atributos += "  + TipoRetorno: \'" + tablaSimbolos.getTabla().get(i).getTipoDevuelto() +"\'\n";

			/* Que es esto?
			 * atributos += "  + EtiqFuncion: \'" + tablaSimbolos.getTabla().get(i).getEtiq() +"\'\n";
			atributos += "  + Param: \'" + tablaSimbolos.getTabla().get(i).getParam() +"\'\n";*/

			atributos += "\n";

			output += lineaLexema + atributos;
		}
		
		if (TablaSimbolosGlobal==tablaSimbolos)
			TablaSimbolosGlobal=null;
		else 
			TablaSimbolosLocal=null;
		
		try {
			bwTS.write(output);
			bwTS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class TablaDecisionLR {
		private String tabla[][] = {
				{"","","","r50","","","","","","","","","","r50","","","","r50","r50","","r50","r50","r50","","r50","100","","","","","","","","","","","","","","","","","","","","0","","","","","","",""},
				{"","","","d7","","","","","","","","","","d101","","","","d8","d9","","d11","d102","d10","","r49","","1","2","","3","","","","","4","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r0","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d5","","","","d8","d9","","d11","d6","d10","","r49","","12","2","","3","","","","","4","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d5","","","","d8","d9","","d11","d6","d10","","r49","","13","2","","3","","","","","4","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d5","","","","d8","d9","","d11","d6","d10","","r49","","14","2","","3","","","","","4","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","d16","d18","d17","","","","","","","","","","","","15","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r9","","","","","","","","","","","d16","d18","d17","","","","","","","","","","","","20","","19","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d22","","","","","d21","d23","","","","","","","","","","","","","","","","","","","","","","","39","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d24","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d25","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d26","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r35","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","27","28","29","30","32","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r1","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r2","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r3","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d37","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r5","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r6","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r7","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d103","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r10","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r25","r25","r25","r25","","","","r25","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","r27","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","40","","","","","41","29","30","32","","","","","","","",""},
				{"","","","r24","r24","r24","r24","","","","r24","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","42","29","30","32","","","","","","","",""},
				{"","","","d43","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","44","29","30","32","","","","","","","",""},
				{"d45","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r34","","","","","","","","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r37","","","","","","","r37","d47","r37","","","","","","","","","","r37","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r39","","","","","","","r39","r39","r39","","","","","","","","","","r39","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","48","","","","","","","",""},
				{"r41","","","","","","","r41","r41","r41","","","","","","","","","","r41","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","49","29","30","32","","","","","","","",""},
				{"r43","","","","","","d50","r43","r43","r43","","","","","","","","","","r43","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r45","","","","","","","r45","r45","r45","","","","","","","","","","r45","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r46","","","","","","","r46","r46","r46","","","","","","","","","","r46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"d98","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d51","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","52","29","30","32","","","","","","","",""},
				{"","","","","","","","d53","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r28","","d46","","","","","","","","","","d55","","","","","","","","","","","","","","","","","","54","","","","","","","","","","","","","","",""},
				{"","","","","","","","d56","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d57","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d58","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r23","r23","","","","","","","","","","r23","","","","r23","r23","","r23","r23","r23","","r23","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","59","30","32","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","60","32","","","","","","","",""},
				{"r40","","","","","","","r40","r40","r40","","","","","","","","","","r40","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d61","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","r27","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","62","","","","","63","29","30","32","","","","","","","",""},
				{"","","","","","","","r12","","","","","","","d16","d18","d17","","","","","","","","","","","","65","","","64","","","","","","","","","","","","","","","","","","","","",""},
				{"d66","","","","","","","","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"d67","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r26","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","68","29","30","32","","","","","","","",""},
				{"d69","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"d70","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","d72","","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","73","","","","71","","","","","","","","","","","","","",""},
				{"r36","","","","","","","r36","d47","r36","","","","","","","","","","r36","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r38","","","","","","","r38","r38","r38","","","","","","","","","","r38","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r42","","","","","","","r42","r42","r42","","","","","","","","","","r42","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d74","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r28","","d46","","","","","","","","","","d55","","","","","","","","","","","","","","","","","","54","","","","","","","","","","","","","","",""},
				{"","","","","","","","d104","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d105","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r18","r18","","","","","","","","","","r18","","","","r18","r18","","r18","r18","r18","","r18","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r19","r19","","","","","","","","","","r19","","","","r19","r19","","r19","r19","r19","","r19","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r28","","d46","","","","","","","","","","d55","","","","","","","","","","","","","","","","","","77","","","","","","","","","","","","","","",""},
				{"","","r20","r20","","","","","","","","","","r20","","","","r20","r20","","r20","r20","r20","","r20","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r21","r21","","","","","","","","","","r21","","","","r21","r21","","r21","r21","r21","","r21","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r22","r22","","","","","","","","","","r22","","","","r22","r22","","r22","r22","r22","","r22","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","79","","","","","","","","","","","","","","","","","","78"},
				{"","","r31","r31","","","","","","","","","","r31","","","","r31","r31","","r31","r31","r31","","r31","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r44","","","","","","","r44","r44","r44","","","","","","","","","","r44","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","d80","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r13","","","","","","","","","","","","d82","","","","","","","","","","","","","81","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r29","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","d83","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r48","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","79","","","","","","","","","","","","","","","","","","99"},
				{"","","r17","d7","","","","","","","","","","d5","","","","d8","d9","","d11","","d10","","","","","85","","","","","","84","86","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r11","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","d16","d18","d17","","","","","","","","","","","","87","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r33","r33","","","","","","","","","","r33","","","","r33","r33","","r33","r33","r33","d89","r33","","","","","","","","","","","","","","","88","","","","","","","","","","","","",""},
				{"","","d90","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r17","d7","","","","","","","","","","d5","","","","d8","d9","","d11","","d10","","","","","","","","","","","96","86","","","","","","","","","","","","","","","","","",""},
				{"","","r17","d7","","","","","","","","","","d5","","","","d8","d9","","d11","","d10","","","","","85","","","","","","97","86","","","","","","","","","","","","","","","","","",""},
				{"","","","d106","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r30","r30","","","","","","","","","","r30","","","","r30","r30","","r30","r30","r30","","r30","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","d92","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r8","","","","","","","","","","r8","","","","r8","r8","","r8","r8","r8","","r8","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r13","","","","","","","","","","","","d82","","","","","","","","","","","","","93","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","79","","","","","","","","","","","","","","","","","","94"},
				{"","","","","","","","r14","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","d95","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r32","r32","","","","","","","","","","r32","","","","r32","r32","","r32","r32","r32","","r32","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r15","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r16","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r4","r4","","","","","","","","","","r4","","","","r4","r4","","r4","r4","r4","","r4","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r47","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","a","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","r51","r51","r51","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","5","","","","","",""},
				{"","","","r52","","","","","","","","","","","r52","r52","r52","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","6","","","","",""},
				{"","","","","","","r53","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","38","","","",""},
				{"","r54","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","75","","",""},
				{"","","","","","","","r55","","","","","","","","","","","","r55","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","76","",""},
				{"","","","","","","","r56","","","","","","","","","","","","r56","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","91",""}
		};

		public TablaDecisionLR() {

		}

		public String accion(int estado, Token<?> token) {
			return tabla[estado+1][token2int(token)];
		}

		public int goto_(int estado, String noTerminal) {
			return Integer.parseInt(tabla[estado+1][noTerm2int(noTerminal)]);
		}
	}

	//Pasar de token a int para la columna de la tabla de decision y para meterlo en la pila de trabajo
	public static int token2int(Token<?> token) {
		int salida = 0; //SE DEVUELVE SALIDA-1
		switch(token.getCodToken()) {
		case "PuntoComa":
			salida = 1;
			break;
		case "CorcheteAbrir":
			salida = 2;
			break;
		case "CorcheteCerrar":
			salida = 3;
			break;
		case "ID":
			salida = 4;
			break;
		case "ENT":
			salida = 5;
			break;
		case "CAD":
			salida = 6;
			break;
		case "ParentesisAbrir":
			salida = 7;
			break;
		case "ParentesisCerrar":
			salida = 8;
			break;
		case "SUMA":
			salida = 9;
			break;
		case "MENOR":
			salida = 10;
			break;
		case "NOT":
			salida = 11;
			break;
		case "ASIG":
			salida = 12;
			break;
		case "ASIGOR":
			salida = 13;
			break;
		case "DEC":
			salida = 14;
			break;
		case "TipoVarENT":
			salida = 15;
			break;
		case "TipoVarLOG":
			salida = 16;
			break;
		case "TipoVarCAD":
			salida = 17;
			break;
		case "Print":
			salida = 18;
			break;
		case "Input":
			salida = 19;
			break;
		case "Coma":
			salida = 20;
			break;
		case "Return":
			salida = 21;
			break;
		case "DECFunc":
			salida = 22;
			break;
		case "IF":
			salida = 23;
			break;
		case "ELSE":
			salida = 24;
			break;
		case "$":
			salida = 25;
			break;
		}
		return salida-1;
	}
	
	//Para la columna de la tabla de decision y para meterlo en la pila de trabajo
	private static int noTerm2int(String noTerminal) {
		int salida = -1;
		switch(noTerminal) {
		case "P'":
			salida = 25;
			break;
		case "P":
			salida = 26;
			break;
		case "D":
			salida = 27;
			break;
		case "T":
			salida = 28;
			break;
		case "F":
			salida = 29;
			break;
		case "T'":
			salida = 30;
			break;
		case "A":
			salida = 31;
			break;
		case "K":
			salida = 32;
			break;
		case "C":
			salida = 33;
			break;
		case "S":
			salida = 34;
			break;
		case "L":
			salida = 35;
			break;
		case "M":
			salida = 36;
			break;
		case "Q":
			salida = 37;
			break;
		case "S'":
			salida = 38;
			break;
		case "G":
			salida = 39;
			break;
		case "X":
			salida = 40;
			break;
		case "E":
			salida = 41;
			break;
		case "U":
			salida = 42;
			break;
		case "R":
			salida = 43;
			break;
		case "V":
			salida = 44;
			break;
		case "MM1":
			salida = 45;
			break;
		case "MM2":
			salida = 46;
			break;
		case "MM3":
			salida = 47;
			break;
		case "MM4":
			salida = 48;
			break;
		case "MM5":
			salida = 49;
			break;
		case "MM6":
			salida = 50;
			break;
		case "MM7":
			salida = 51;
			break;
		case "S''":
			salida = 52;
			break;
		}
		return salida;
	}

	public static class Gramatica{
		private String[] antecedentes = {"P''", "P'", "P", "P", "P", "D", "T", "T", "T", "F", "T'", "T'", "A", "A", "K", "K", "C", "C", "C",
				"S", "S", "S", "S", "S", "S", "L", "L", "M", "M", "Q", "Q", "S'", "S'", "G", "G", "X", "X", "E", "E", "U", "U", "R", "R",
				"V", "V", "V", "V", "V", "S''", "S''", "P", "MM1", "MM2", "MM3", "MM4", "MM5", "MM6", "MM7"};

		private int[] longitudes = {1, 2, 2, 2, 2, 5, 1, 1, 1, 12, 0, 1, 4, 0, 0, 5, 2, 2, 0, 4, 5, 5, 5, 5, 3, 1, 1, 2, 0, 0, 3, 4, 1,
				4, 0, 1, 0, 3, 1, 3, 1, 2, 1, 3, 1, 4, 1, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0};

		public Gramatica(){

		}

		public int getLongitud(int numRegla) {
			//+1 ya que P''->P' es la regla -1
			return longitudes[numRegla+1];
		}

		public String getAntecedente(int numRegla) {
			return antecedentes[numRegla+1];
		}
	}

	public static void errorSintactico(int estado) {
		switch (estado) {
		case 0:
		case 4:
		case 7: 
		case 14:
		case 85: 
		case 86: 
		case 96: 
		case 97:
			gestorErrores(ERR_ST,1);
			break;
		case 1:
			gestorErrores(ERR_ST,-1);
			break;
		case 2: 
		case 5: 
		case 12:
		case 15: 
		case 37:
		case 98:
			gestorErrores(ERR_ST,2);
			break;

		case 3:
		case 6:
		case 13:
		case 19:
		case 38:
		case 51: 
		case 64: 
		case 65:
		case 75:
		case 76:
		case 80:
		case 81:
		case 82:
		case 84:
		case 87:
		case 90:
		case 91:
		case 93:
			gestorErrores(ERR_ST, 3);
			break;
		case 8:
		case 24: 
		case 42: 
		case 56: 
		case 69: 
			gestorErrores(ERR_ST, 4);
			break;
		case 9:
		case 25:
		case 43:
		case 57:
		case 70:
			gestorErrores (ERR_ST, 5);
			break;
		case 10:
		case 26:
		case 44:
		case 58: 
		case 71:
		case 73:
			gestorErrores (ERR_ST, 6);
			break;

		case 11:
		case 27:
		case 28:
		case 45:
			gestorErrores (ERR_ST, 7);
			break;

		case 16:
		case 17:
		case 18:
		case 20: 
			gestorErrores (ERR_ST, 8);
			break;

		case 21:
		case 23:
		case 39:
		case 52:
		case 66:
			gestorErrores (ERR_ST, 9);
			break;
		case 22:
		case 40:
		case 41:
		case 53:
		case 54:
		case 55:
		case 63:
		case 67:
		case 68:
		case 77:
			gestorErrores (ERR_ST, 10);
			break;
		case 29:
		case 30:
		case 31:
		case 32:
		case 33:
		case 34:
		case 35:
		case 36:
		case 46:
		case 47:
		case 48:
		case 49:
		case 50:
		case 59:
		case 60:
		case 61:
		case 62:
		case 74:
			gestorErrores (ERR_ST, 11);
			break;
		case 72:
		case 78:
		case 79:
		case 83:
		case 88:
		case 89:
		case 92:
		case 94:
		case 95:
		case 99: 
			gestorErrores (ERR_ST, 12);
			break;
		}
	}


	public static void gestorErrores(int tipo, int error) {
		String msg = "Error";
		if (tipo == ERR_LEX){
			msg += " Lexico: ";
			switch (error) {
			case 1:
				msg += "Transicion no prevista.";
				break;
			case 2:
				msg += "Numero fuera de rango.";
				break;
			case 3:
				msg += "Exceso de caracteres en la cadena.";
				break;
			case 4:
				msg += "Variable ya declarada.";
				break;
			}
		} else if (tipo == ERR_ST) {
			msg += " Sintactico: ";
			switch(error) {
			case -1:
				msg += "No se pudo derivar la raiz."; 
				break;
			case 1:
				msg += "Sentencia no valida.";
				break;
			case 2: 
				msg += "Declaracion incorrecta de variable.";
				break;
			case 3:
				msg += "Declaracion incorrecta de funcion.";
				break;
			case 4:
				msg += "Sentencia print incorrecta.";
				break;
			case 5:
				msg += "Sentencia input incorrecta.";
				break;
			case 6:
				msg += "Sentencia condicional simple incorrecta.";
				break;
			case 7:
				msg += "Sentencia return incorrecta.";
				break;
			case 8:
				msg += "Tipo incorrecto.";
				break;
			case 9:
				msg += "Asignacion incorrecta.";
				break;
			case 10:
				msg += "Llamada a funcion incorrecta.";
				break;
			case 11:
				msg += "Expresion incorrecta.";
				break;
			case 12:
				msg += "Sentencia condicional compuesta incorrecta."; 
				break;
			} 
		} else if (tipo == ERR_SE) {
			msg += " Semantico: ";
			switch(error) {
			
			}
		}
		
		msg += "  Linea: " + linea;

		System.out.println(msg);
		liberaTS(TablaSimbolosActual);
		liberaTS(TablaSimbolosGlobal);
		try {
			br.close();
			bw.close();
			bwTS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(1);
	}

	public static void meterEnPilaSemanticaToken(int numToken, Token<?> token) {
		ElemSem nuevoElem = new ElemSem();
		switch(numToken) {
			case 3:
				nuevoElem.setPosi((String) token.getAtributo());//El atributo aqui deberia ser la posicion segun el codigo que tenemos en ALex()
				break;
			case 4:
				nuevoElem.setCadena((String) token.getAtributo());
				break;
			case 5:
				nuevoElem.setNum((int) token.getAtributo());
				break;
		}
		pilaSem.push(nuevoElem);
	}
	
	
	public static void accionSemantica(int numRegla) {
		ElemSem tope, tope1, tope2, tope3, tope4, tope5, tope6, tope7, tope8, tope9;
		ElemSem nuevoElem = new ElemSem();
		int posi;
		switch(numRegla) {
		case 0:
			liberaTS(TablaSimbolosGlobal); //TODO
			break;
		case 1:
		case 2:
		case 15:
			tope = pilaSem.pop();
			pilaSem.pop();
			nuevoElem.setTipoRet(tope.getTipoRet());
			break;
		case 3:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			if (tope1.getTipoRet().equals("tipo_vacio"))
				nuevoElem.setTipoRet(tope.getTipoRet());
			else 
				gestorErrores(ERR_SE,1);
			break;
		case 4:
			pilaSem.pop();
			tope1 = pilaSem.pop();
			tope2 = pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			posi = Integer.parseInt(tope1.getPosi().substring(1));
			TablaSimbolosActual.getTabla().get(posi).setTipo(tope2.getTipo());
			if (TablaSimbolosActual == TablaSimbolosGlobal) {
				TablaSimbolosActual.getTabla().get(posi).setDesplazamiento(desplG);
				desplG += tope2.getTamano();
			} else {
				TablaSimbolosActual.getTabla().get(posi).setDesplazamiento(desplL);
				desplL += tope2.getTamano();
			}
			zona_decl = false;
			break;
		case 5:
			pilaSem.pop();
			nuevoElem.setTipo("entero");
			nuevoElem.setTamano(1);
			break;
		case 6:
			pilaSem.pop();
			nuevoElem.setTipo("cadena");
			nuevoElem.setTamano(1);
			break;
		case 7:
			pilaSem.pop();
			nuevoElem.setTipo("logico");
			nuevoElem.setTamano(1);
			break;
		case 8:
			tope1=null;
			tope9=null;
			for(int i =0; i<12;i++){
				if(i==1) tope1=pilaSem.pop();
				else if(i==9) tope9=pilaSem.pop();
				else pilaSem.pop();
			}
			if(tope1!=null && tope9!=null && !tope1.getTipoRet().equals(tope9.getTipoRet())) gestorErrores(ERR_SE,2);
			TablaSimbolosActual = TablaSimbolosGlobal;
			liberaTS(TablaSimbolosGlobal);
			break;
		case 9:
		case 12:
		case 13:
			nuevoElem.setTipo("tipo_vacio");
			break;
		case 10:
			tope=pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
			break;
		case 11:
			tope = pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			tope3 = pilaSem.pop();
			if (tope.getTipo().equals("tipo_vacio")) 
				nuevoElem.setTipo(tope3.getTipo());
			else
				nuevoElem.setTipoLista(tope3.getTipo());
			break;
		case 14:
			tope = pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			tope3 = pilaSem.pop();
			pilaSem.pop();
			/*
			 * NO ES NECESARIO
			 * 
			 if (tope.getTipo().equals("tipo_vacio")) 
				nuevoElem.setTipoLista(tope3.getTipo()); 
			else
				nuevoElem.setTipoLista(tope3.getTipo());
			 */
			nuevoElem.setTipoLista(tope3.getTipo()); //<-- este ya crea ademas de añadir
			break;
		case 16:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			if (tope1.getTipoRet().equals(tope.getTipoRet()))
				nuevoElem.setTipoRet(tope1.getTipoRet());
			else if (tope1.getTipoRet().equals("tipo_vacio"))
				nuevoElem.setTipoRet(tope.getTipoRet());
			else if (tope.getTipoRet().equals("tipo_vacio"))
				nuevoElem.setTipoRet(tope1.getTipoRet());
			else 
				gestorErrores(ERR_SE,3);
			break;
		case 17:
			nuevoElem.setTipoRet("tipo_vacio");
			break;
		case 18:
			pilaSem.pop();
			tope1=pilaSem.pop();
			pilaSem.pop();
			tope3 = pilaSem.pop();
			//TODO
			nuevoElem.setTipoRet("tipo_vacio");
			break;
		case 19:
			pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			//TODO
			nuevoElem.setTipoRet("tipo_vacio");
			break;
		case 20:
			pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			
			if (tope2.getTipo().equals("entero") || tope2.getTipo().equals("cadena"))
				nuevoElem.setTipo("tipo_ok");
			else 
				gestorErrores(ERR_SE,5);
			
			nuevoElem.setTipoRet("tipo_vacio");
			break; 
		
		case 21:
			pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			
			if(buscaTipoTS(tope2.getPosi()).equals("entero") || buscaTipoTS(tope2.getPosi()).equals("cadena"))
				nuevoElem.setTipo("tipo_ok");
			else
				gestorErrores(ERR_SE,6);
				
			nuevoElem.setTipoRet("tipo_vacio");
		break;
			
		case 22:
			tope=pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			if(tope2.getTipo().equals("logico"))
				nuevoElem.setTipo(tope.getTipo());
			else
				gestorErrores(ERR_SE,7);
			nuevoElem.setTipoRet(tope.getTipoRet());
		break;
				
		case 23:
			//TODO
		break;
				
		case 24:
		
		break;
		
		case 25:
		
		break;
		
		case 26:
			tope=pilaSem.pop();
			tope1=pilaSem.pop();
			if(!tope1.getTipo().equals("tipo_error") && !tope.getTipo().equals("tipo_error")) //!
				if(tope.getTipo().equals("tipo_vacio"))
					nuevoElem.setTipo(tope1.getTipo());
				else
					//TODO
				
			//else
				gestorErrores(ERR_SE,10);
				
		break;
				
		case 27:
			nuevoElem.setTipo("tipo_vacio");
		break;
		
		case 28:
			nuevoElem.setTipo("tipo_vacio");
		break;
		
		case 29:
			tope=pilaSem.pop();
			tope1=pilaSem.pop();
			pilaSem.pop();
			if(!tope1.getTipo().equals("tipo_error") && !tope.getTipo().equals("tipo_error")) //!
				if(tope.getTipo().equals("tipo_vacio")){
					pilaSem.pop();
					nuevoElem.setTipo(pilaSem.pop().getTipo());
				}
				else
							//TODO

			//else
				gestorErrores(ERR_SE,11);
		break;
				
		case 30:
			tope=pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			pilaSem.pop();
			if(!tope2.getTipo().equals("tipo_error"))
				if(!tope.getTipo().equals("tipo_error"))
					nuevoElem.setTipo(tope2.getTipo());
				else
					gestorErrores(ERR_SE,13);
			else
				gestorErrores(ERR_SE,12);
				
			if(tope2.getTipoRet().equals(tope.getTipoRet()))
				nuevoElem.setTipoRet(tope.getTipoRet());
				
			else
				//ERROR
				
		break;
				
		case 31:
			tope=pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
			nuevoElem.setTipoRet(tope.getTipoRet());
		break;
		
		case 32:
			pilaSem.pop();
			tope1=pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			nuevoElem.setTipo(tope1.getTipo());
			nuevoElem.setTipoRet(tope1.getTipoRet());
		break;
		
		case 33:
			nuevoElem.setTipo("tipo_vacio");
			nuevoElem.setTipoRet("tipo_vacio");
		break;
		
		case 34:
			tope=pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
		break;
				
		case 35:
			nuevoElem.setTipo("tipo_vacio");
		break;
				
		case 36:
			tope=pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			if(tope.getTipo().equals(tope.getTipo()) && tope.getTipo().equals("entero"))
				nuevoElem.setTipo("logico");	
			else
				gestorErrores(ERR_SE,14);
				
		break;
				
		case 37:
			tope=pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
		break;
				
		case 38:
			tope=pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			if(tope2.getTipo().equals(tope2.getTipo()) && tope2.getTipo().equals("entero"))
				nuevoElem.setTipo("entero");
			else
				gestorErrores(ERR_SE,15);
				
		break;
				
		case 39:
			tope=pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
		break;
		
		case 40:
			tope = pilaSem.pop();
			pilaSem.pop();
			if (tope.getTipo().equals("logico")) {
				nuevoElem.setTipo("logico");
			} else {
				gestorErrores(ERR_SE,16);
			}
			break;
			
		case 41:
			tope = pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
			break;
			
		case 42:
			pilaSem.pop();
			tope1 = pilaSem.pop();
			pilaSem.pop();
			nuevoElem.setTipo(tope1.getTipo());
			break;
			
		case 43:
			tope = pilaSem.pop();
			nuevoElem.setTipo(buscaTipoTS(tope.getPosi()));
			break;
		
		case 44:
			pilaSem.pop();
			tope1 = pilaSem.pop();
			pilaSem.pop();
			tope3 = pilaSem.pop();
			String[] tipos = {"entero", "logico", "cadena", "tipo_vacio"};
			for (String t : tipos ) {		
				if (buscaTipoTS(tope3.getPosi()) == porFunc(tope1.getTipo(), t)){
					nuevoElem.setTipo(t); //TODO porFunc
				} else {
					gestorErrores(ERR_SE,17);
				}
			}
			break;
		
		case 45:
			pilaSem.pop();
			nuevoElem.setTipo("entero");
			break;
			
		case 46:
			pilaSem.pop();
			nuevoElem.setTipo("cadena");
			break;
		
		case 47:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			if (!tope1.getTipo().equals("tipo_error")) {
				nuevoElem.setTipo(tope.getTipo());
			} else {
				gestorErrores(ERR_SE, 18);
			}
			
			if (tope1.getTipoRet().equals("tipo_vacio")) {
				nuevoElem.setTipoRet(tope.getTipoRet());
			} else if (tope.getTipoRet().equals("tipo_vacio")) {
				nuevoElem.setTipoRet(tope1.getTipoRet());
			} else {
				gestorErrores(ERR_SE, 19);
			}
			break;
		
		case 48:
			tope = pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
			nuevoElem.setTipoRet(tope.getTipoRet());
			break;
		
		case 49:
			nuevoElem.setTipoRet("tipo vacio");
			break;
		
		case 50:
			TablaSimbolosGlobal = new TS("G");
			desplG = 0;
			TablaSimbolosActual = TablaSimbolosGlobal;
			break;
			
		case 51:
			zona_decl = false;
			break;
			
		case 52:
			zona_decl = true;
			break;
			
		case 53:
			TablaSimbolosLocal = new TS("L");
			desplL = 0;
			TablaSimbolosActual = TablaSimbolosLocal;
			break;
			
		case 54:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			tope2 = pilaSem.pop();
			tope3 = pilaSem.pop();
			tope4 = pilaSem.pop();
			tope5 = pilaSem.pop();
			tope6 = pilaSem.pop();
			tope7 = pilaSem.pop();
			tope8 = pilaSem.pop();
			tope9 = pilaSem.pop();
			posi = Integer.parseInt(tope8.getPosi().substring(1));
			TablaSimbolosActual.getTabla().get(posi).setTipo(porFunc(tope5.getTipo(), tope9.getTipo()));
			zona_decl = false;
			pilaSem.push(tope9);
			pilaSem.push(tope8);
			pilaSem.push(tope7);
			pilaSem.push(tope6);
			pilaSem.push(tope5);
			pilaSem.push(tope4);
			pilaSem.push(tope3);
			pilaSem.push(tope2);
			pilaSem.push(tope1);
			pilaSem.push(tope);
			break;
			
		case 55:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			tope2 = pilaSem.pop();
			tope3 = pilaSem.pop();
			posi = Integer.parseInt(tope2.getPosi().substring(1));
			TablaSimbolosActual.getTabla().get(posi).setTipo(tope3.getTipo());
			TablaSimbolosActual.getTabla().get(posi).setDesplazamiento(desplL);
			desplL += tope2.getTamano();
			pilaSem.push(tope3);
			pilaSem.push(tope2);
			pilaSem.push(tope1);
			pilaSem.push(tope);
			break;
			
		case 56:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			tope2 = pilaSem.pop();
			tope3 = pilaSem.pop();
			posi = Integer.parseInt(tope2.getPosi().substring(1));
			TablaSimbolosActual.getTabla().get(posi).setTipo(tope3.getTipo());
			TablaSimbolosActual.getTabla().get(posi).setDesplazamiento(desplL);
			desplL += tope2.getTamano();
			pilaSem.push(tope3);
			pilaSem.push(tope2);
			pilaSem.push(tope1);
			pilaSem.push(tope);
			break;
		}
		
		pilaSem.push(nuevoElem);
	}
	
	public static void main(String []args){
		//File file = new File(args[0]);
		File file = new File("PIdG82 (1).txt");
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
		bwTS = null;	

		/*lee();
		while(car!='\0') ALex();*/

		pilaSt.push(0);
		String parse = "A ";
		lee();
		Token<?> token = ALex();
		while(true) {
			int s = pilaSt.peek();
			String accion = TDecLR.accion(s, token);
			if (accion == "") {
				errorSintactico(s);
				break;
			} else if (accion.charAt(0) == 'd') {
				pilaSt.push(token2int(token));
				pilaSt.push(Integer.parseInt(accion.substring(1)));
				meterEnPilaSemanticaToken(token2int(token), token); // <-----
				token = ALex();
			} else if (accion.charAt(0) == 'r') {
				int numRegla = Integer.parseInt(accion.substring(1));
				for (int i = 0; i < 2*gramatica.getLongitud(numRegla); i++) {
					pilaSt.pop();
				}
				int s2 = pilaSt.peek();
				pilaSt.push(noTerm2int(gramatica.getAntecedente(numRegla)));
				int nuevoEstado = TDecLR.goto_(s2, gramatica.getAntecedente(numRegla));
				pilaSt.push(nuevoEstado);
				parse += (numRegla) + " ";
				accionSemantica(numRegla); // <-----
			} else if (accion.charAt(0) == 'a') {
				//FIN, pero si pongo return no llega a escribirTablaSimbolos
				break;
			}
		}
		
		bwSt = null;
		try {
			bwSt = new BufferedWriter(new FileWriter("parseDer.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		try {
			bwSt.write(parse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//escribirTablaSimbolos(TablaSimbolosGlobal);

		try {
			br.close();
			bw.close();
			bwTS.close();
			bwSt.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}