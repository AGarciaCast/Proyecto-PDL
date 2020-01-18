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
	static boolean zona_decl;

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

		public void anadirTipoLista(String tipo) {
			if (this.tipoLista==null) this.tipoLista=new ArrayList<String>();
			this.tipoLista.add(tipo);
		}

		public void setTipoLista(List<String> lista, String tipo) {
			this.tipoLista = lista;
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

		public TSElem get(int pos) {
			return tabla.get(pos);
		}

		public int size() {
			return tabla.size();
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
			TS tabla = TablaSimbolosLocal;
			while (posTS<tabla.size() && !tabla.get(posTS).getLexema().equals(lexema)){
				posTS++;
			}
			resultado= posTS == tabla.size() ? null : TablaSimbolosLocal.getId()+posTS;
		}
		if(resultado == null){
			int posTS=0;
			TS tabla = TablaSimbolosGlobal;
			while (posTS<tabla.size() && !tabla.get(posTS).getLexema().equals(lexema)){
				posTS++;
			}
			resultado= posTS == tabla.size() ? null : TablaSimbolosGlobal.getId()+posTS;
		}
		return resultado;
	}


	public static String buscaTipoTS(String posi){
		int p = Integer.parseInt(posi.substring(1));
		String tipo;
		if (posi.charAt(0) == 'G') {
			tipo = TablaSimbolosGlobal.get(p).getTipo();
			if (tipo == "funcion") {
				List<String> lista = new ArrayList<>(Arrays.asList(TablaSimbolosGlobal.get(p).getTipoArgs()));
				tipo = parFunc(lista, TablaSimbolosGlobal.get(p).getTipoDevuelto());
			}
		} else {
			tipo = TablaSimbolosActual.get(p).getTipo();
			if (tipo == "funcion") {
				List<String> lista = new ArrayList<>(Arrays.asList(TablaSimbolosActual.get(p).getTipoArgs()));
				tipo = parFunc(lista, TablaSimbolosActual.get(p).getTipoDevuelto());
			}
		}
		return tipo;
	}

	//Copia los argumentos a un string con el tipo de retorno al final
	public static <E> String parFunc(E listaArgs, String tipoRetorno) {
		String salida = "";
		if(listaArgs instanceof List<?>){
			for (int i = 0; i < ((List<String>) listaArgs).size()-1; i++) {
				salida += ((List<String>) listaArgs).get(i) + " x ";
			}
			salida += ((List<String>) listaArgs).get(((List<String>) listaArgs).size()-1);
		}else if(listaArgs instanceof String){
			salida += listaArgs;
		}else
			return null;
		salida += " --> " + tipoRetorno;
		return salida;
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
		private String etiqueta;

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

		public String[] getTipoArgs() {
			return tipoArgs;
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

		public void setEtiq(String str) {
			this.etiqueta = str;
		}

		public String getEtiq() {
			return etiqueta;
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
				case '\r':
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
					TablaSimbolosGlobal.get(p).setTipo("entero");
					TablaSimbolosGlobal.get(p).setDesplazamiento(desplG);
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

		String cabecera = "Tabla Simbolos #" + tablaSimbolos.getNum() + ":\n";
		String output = cabecera + "\n";

		for (int i = 0; i < tablaSimbolos.size(); i++) {
			String lineaLexema = " * LEXEMA: \'" + tablaSimbolos.get(i).getLexema() + "\'\n";

			String atributos = "";

			if (tablaSimbolos.get(i).getTipo() != null) 
				atributos += "  + Tipo: \'" + tablaSimbolos.get(i).getTipo() +"\'\n";

			if (tablaSimbolos.get(i).getDesplazamiento() != -1)
				atributos += "  + Despl: " + tablaSimbolos.get(i).getDesplazamiento() +"\n";

			if (tablaSimbolos.get(i).getNArgs() != -1){
				if (tablaSimbolos.get(i).getTipoArgs(0) != null && tablaSimbolos.get(i).getTipoArgs(0).equals("tipo_vacio")) {
					atributos += "  + numParam: 0\n";
				} else {
					atributos += "  + numParam: " + tablaSimbolos.get(i).getNArgs() +"\n";
				}
			}

			for (int j = 0; j < tablaSimbolos.get(i).getNArgs(); j++) {
				if (tablaSimbolos.get(i).getTipoArgs(j) != null && !tablaSimbolos.get(i).getTipoArgs(j).equals("tipo_vacio"))
					atributos += "  + TipoParam" + (j+1) + ": \'" + tablaSimbolos.get(i).getTipoArgs(j) +"\'\n";
				//if (tablaSimbolos.get(i).getModoArgs(j) != null)
				atributos += "  + ModoParam" + (j+1) + ": \'Valor\'\n";
			}
			if (tablaSimbolos.get(i).getTipoDevuelto() != null)
				atributos += "  + TipoRetorno: \'" +(tablaSimbolos.get(i).getTipoDevuelto().equals("tipo_vacio") ? "void" : tablaSimbolos.get(i).getTipoDevuelto())+"\'\n";
			
			if (tablaSimbolos.get(i).getTipo().equals("funcion"))
				atributos += "  + EtiqFuncion: \'" + tablaSimbolos.getTabla().get(i).getEtiq() +"\'\n";
			
			//atributos += "  + Param: \'" + tablaSimbolos.getTabla().get(i).getParam() +"\'\n";

			atributos += "\n";

			output += lineaLexema + atributos;
		}

		if (TablaSimbolosGlobal==tablaSimbolos)
			TablaSimbolosGlobal=null;
		else 
			TablaSimbolosLocal=null;

		try {
			bwTS.write(output);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class TablaDecisionLR {
		private String tabla[][] = {
				{"","","","r50","","","","","","","","","","r50","","","","r50","r50","","r50","r50","r50","","r50","100","","","","","","","","","","","","","","","","","","","","0","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d101","","","","d8","d9","","d11","d102","d10","","r49","","1","2","","3","","","","","4","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r0","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d101","","","","d8","d9","","d11","d102","d10","","r49","","12","2","","3","","","","","4","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d101","","","","d8","d9","","d11","d102","d10","","r49","","13","2","","3","","","","","4","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","d101","","","","d8","d9","","d11","d102","d10","","r49","","14","2","","3","","","","","4","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","d16","d18","d17","","","","","","","","","","","","15","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r9","","","","","","","","","","","d16","d18","d17","","","","","","","","","","","","20","","19","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d22","","","","","d21","d23","","","","","","","","","","","","","","","","","","","","","","","39","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d24","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d25","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d26","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r35","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","27","28","29","30","32","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r1","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r2","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","r3","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d107","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r5","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r6","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r7","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d103","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r10","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r25","r25","r25","r25","","","","r25","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","r27","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","40","","","","","41","29","30","32","","","","","","","","",""},
				{"","","","r24","r24","r24","r24","","","","r24","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","42","29","30","32","","","","","","","","",""},
				{"","","","d43","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","44","29","30","32","","","","","","","","",""},
				{"d45","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r34","","","","","","","","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r37","","","","","","","r37","d47","r37","","","","","","","","","","r37","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r39","","","","","","","r39","r39","r39","","","","","","","","","","r39","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","48","","","","","","","","",""},
				{"r41","","","","","","","r41","r41","r41","","","","","","","","","","r41","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","49","29","30","32","","","","","","","","",""},
				{"r43","","","","","","d50","r43","r43","r43","","","","","","","","","","r43","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r45","","","","","","","r45","r45","r45","","","","","","","","","","r45","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r46","","","","","","","r46","r46","r46","","","","","","","","","","r46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"d98","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","d51","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","52","29","30","32","","","","","","","","",""},
				{"","","","","","","","d53","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r28","","d46","","","","","","","","","","d55","","","","","","","","","","","","","","","","","","54","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d56","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d57","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d58","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r23","r23","","","","","","","","","","r23","","","","r23","r23","","r23","r23","r23","","r23","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","59","30","32","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","60","32","","","","","","","","",""},
				{"r40","","","","","","","r40","r40","r40","","","","","","","","","","r40","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d61","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","r27","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","62","","","","","63","29","30","32","","","","","","","","",""},
				{"","","","","","","","r12","","","","","","","d16","d18","d17","","","","","","","","","","","","65","","","64","","","","","","","","","","","","","","","","","","","","","",""},
				{"d66","","","","","","","","","d46","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"d67","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r26","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d34","d35","d36","d33","","","","d31","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","68","29","30","32","","","","","","","","",""},
				{"d69","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"d70","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","d72","","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","73","","","","71","","","","","","","","","","","","","","",""},
				{"r36","","","","","","","r36","d47","r36","","","","","","","","","","r36","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r38","","","","","","","r38","r38","r38","","","","","","","","","","r38","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r42","","","","","","","r42","r42","r42","","","","","","","","","","r42","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d74","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r28","","d46","","","","","","","","","","d55","","","","","","","","","","","","","","","","","","54","","","","","","","","","","","","","","","",""},
				{"","","","","","","","d104","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d105","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r18","r18","","","","","","","","","","r18","","","","r18","r18","","r18","r18","r18","","r18","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r19","r19","","","","","","","","","","r19","","","","r19","r19","","r19","r19","r19","","r19","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r28","","d46","","","","","","","","","","d55","","","","","","","","","","","","","","","","","","77","","","","","","","","","","","","","","","",""},
				{"","","r20","r20","","","","","","","","","","r20","","","","r20","r20","","r20","r20","r20","","r20","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r21","r21","","","","","","","","","","r21","","","","r21","r21","","r21","r21","r21","","r21","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r22","r22","","","","","","","","","","r22","","","","r22","r22","","r22","r22","r22","","r22","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","79","","","","","","","","","","","","","","","","","","","78"},
				{"","","r31","r31","","","","","","","","","","r31","","","","r31","r31","","r31","r31","r31","","r31","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"r44","","","","","","","r44","r44","r44","","","","","","","","","","r44","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","d80","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r13","","","","","","","","","","","","d82","","","","","","","","","","","","","81","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r29","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","d83","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r48","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","79","","","","","","","","","","","","","","","","","","","99"},
				{"","","r17","d7","","","","","","","","","","d101","","","","d8","d9","","d11","","d10","","","","","85","","","","","","84","86","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r11","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","d16","d18","d17","","","","","","","","","","","","87","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r33","r33","","","","","","","","","","r33","","","","r33","r33","","r33","r33","r33","d89","r33","","","","","","","","","","","","","","","88","","","","","","","","","","","","","",""},
				{"","","d90","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r17","d7","","","","","","","","","","d101","","","","d8","d9","","d11","","d10","","","","","85","","","","","","96","86","","","","","","","","","","","","","","","","","","",""},
				{"","","r17","d7","","","","","","","","","","d101","","","","d8","d9","","d11","","d10","","","","","85","","","","","","97","86","","","","","","","","","","","","","","","","","","",""},
				{"","","","d106","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r30","r30","","","","","","","","","","r30","","","","r30","r30","","r30","r30","r30","","r30","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","d92","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","r8","","","","","","","","","","r8","","","","r8","r8","","r8","r8","r8","","r8","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","r13","","","","","","","","","","","","d82","","","","","","","","","","","","","93","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","d7","","","","","","","","","","","","","","d8","d9","","d11","","d10","","","","","","","","","","","","79","","","","","","","","","","","","","","","","","","","94"},
				{"","","","","","","","r14","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","d95","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r32","r32","","","","","","","","","","r32","","","","r32","r32","","r32","r32","r32","","r32","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r15","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r16","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r4","r4","","","","","","","","","","r4","","","","r4","r4","","r4","r4","r4","","r4","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","r47","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","","","","","","","","","","","a","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""},
				{"","","","","","","","","","","","","","","r51","r51","r51","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","5","","","","","","",""},
				{"","","","r52","","","","","","","","","","","r52","r52","r52","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","6","","","","","",""},
				{"","","","","","","r53","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","38","","","","",""},
				{"","r54","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","75","","","",""},
				{"","","","","","","","r55","","","","","","","","","","","","r55","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","76","","",""},
				{"","","","","","","","r56","","","","","","","","","","","","r56","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","91","",""},
				{"r57","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","37",""}
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
		case "MM8":
			salida = 52;
			break;
		case "S''":
			salida = 53;
			break;
		}
		return salida;
	}

	public static class Gramatica{
		private String[] antecedentes = {"P''", "P'", "P", "P", "P", "D", "T", "T", "T", "F", "T'", "T'", "A", "A", "K", "K", "C", "C", "C",
				"S", "S", "S", "S", "S", "S", "L", "L", "M", "M", "Q", "Q", "S'", "S'", "G", "G", "X", "X", "E", "E", "U", "U", "R", "R",
				"V", "V", "V", "V", "V", "S''", "S''", "P", "MM1", "MM2", "MM3", "MM4", "MM5", "MM6", "MM7", "MM8"};

		private int[] longitudes = {1, 2, 2, 2, 2, 6, 1, 1, 1, 12, 0, 1, 4, 0, 0, 5, 2, 2, 0, 4, 5, 5, 5, 5, 3, 1, 1, 2, 0, 0, 3, 4, 1,
				4, 0, 1, 0, 3, 1, 3, 1, 2, 1, 3, 1, 4, 1, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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
			case 1:
				msg += "RETURN fuera de funcion.";
				break;
			case 2:
				msg += "El tipo devuelto no coincide con el declarado en la funcion.";
				break;
			case 3:
				msg += "Asignacion incorrecta.";
				break;
			case 4:
				msg += "Incoherencia entre parametros formales y actuales en la llamada a funcion.";
				break;
			case 5:
				msg += "La condicion del IF no es de tipo logico.";
				break;
			case 6:
				msg += "Error en la sentencia del RETURN.";
				break;
			case 7:
				msg += "Error al definir los parametros de llamada de una funcion.";
				break;
			case 8:
				msg += "Error en el cuerpo del IF.";
				break;
			case 9:
				msg += "Error en el cuerpo del ELSE.";
				break;
			case 10:
				msg += "No concuerdan los RETURN de las sentencias IF-ELSE.";
				break;
			case 11:
				msg += "Tipos incompatibles entre operandos y operadores.";
				break;
			case 12:
				msg += "Error en el cuerpo del IF-ELSE.";
				break;
			case 13:
				msg += "Error en el RETURN en el cuerpo del IF-ELSE.";
				break;
			}
		}

		msg += "  Linea: " + linea;

		System.out.println(msg);
		liberaTS(TablaSimbolosActual);
		if (TablaSimbolosGlobal != null) liberaTS(TablaSimbolosGlobal);
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
		case 5:
			nuevoElem.setCadena((String) token.getAtributo());
			break;
		case 4:
			nuevoElem.setNum((int) token.getAtributo());
			break;
		}
		pilaSem.push(nuevoElem);
	}


	public static void accionSemantica(int numRegla) {
		String[] tipos = {"entero", "logico", "cadena", "tipo_vacio"};
		ElemSem tope, tope1, tope2, tope3, tope4, tope5, tope9;
		ElemSem nuevoElem = new ElemSem();
		int posi;
		switch(numRegla) {
		case 0:
			liberaTS(TablaSimbolosGlobal); 
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
			pilaSem.pop();
			tope2 = pilaSem.pop();
			tope3 = pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			posi = Integer.parseInt(tope2.getPosi().substring(1));
			TablaSimbolosActual.get(posi).setTipo(tope3.getTipo());
			if (TablaSimbolosActual == TablaSimbolosGlobal) {
				TablaSimbolosActual.get(posi).setDesplazamiento(desplG);
				desplG += tope3.getTamano();
			} else {
				TablaSimbolosActual.get(posi).setDesplazamiento(desplL);
				desplL += tope3.getTamano();
			}
			break;
		case 5:
			pilaSem.pop();
			nuevoElem.setTipo("entero");
			nuevoElem.setTamano(1);
			break;
		case 6:
			pilaSem.pop();
			nuevoElem.setTipo("cadena");
			nuevoElem.setTamano(64);
			break;
		case 7:
			pilaSem.pop();
			nuevoElem.setTipo("logico");
			nuevoElem.setTamano(1);
			break;
		case 8:
			tope1=null;
			tope9=null;
			for(int i = 0; i<12;i++){
				if(i==1) tope1=pilaSem.pop();
				else if(i==9) tope9=pilaSem.pop();
				else pilaSem.pop();
			}
			if(tope1!=null && tope9!=null && !tope1.getTipoRet().equals(tope9.getTipo())) gestorErrores(ERR_SE,2);
			TablaSimbolosActual = TablaSimbolosGlobal;
			liberaTS(TablaSimbolosLocal);
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
			if (tope.getTipo() != null && tope.getTipo().equals("tipo_vacio")) 
				nuevoElem.setTipo(tope3.getTipo());
			else
				nuevoElem.setTipoLista(tope.getTipoLista(), tope3.getTipo());
			break;
		case 14:
			tope = pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();
			tope3 = pilaSem.pop();
			pilaSem.pop();
			if (tope.getTipo() != null && tope.getTipo().equals("tipo_vacio")) 
				nuevoElem.anadirTipoLista(tope3.getTipo());
			else
				nuevoElem.setTipoLista(tope.getTipoLista(), tope3.getTipo());
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
				gestorErrores(ERR_SE,2);
			break;
		case 17:
			nuevoElem.setTipoRet("tipo_vacio");
			break;
		case 18:
			pilaSem.pop();
			tope1=pilaSem.pop();
			pilaSem.pop();
			tope3 = pilaSem.pop();
			if(tope1.getTipo().equals(buscaTipoTS(tope3.getPosi())) && !tope1.getTipo().equals("tipo_error"))
				nuevoElem.setTipo("tipo_ok");
			else
				gestorErrores(ERR_SE,3);
			nuevoElem.setTipoRet("tipo_vacio");
			break;
		case 19:
			pilaSem.pop();
			pilaSem.pop();
			tope2 = pilaSem.pop();
			pilaSem.pop();
			tope4 = pilaSem.pop();
			for (String t : tipos) {		
				//Mirar si era realmente una lista...
				boolean condicion = tope2.getTipoLista() != null ? buscaTipoTS(tope4.getPosi()).equals(parFunc(tope2.getTipoLista(), t)) : buscaTipoTS(tope4.getPosi()).equals(parFunc(tope2.getTipo(), t));
				if (condicion){
					nuevoElem.setTipo("tipo_ok");
				}
			}
			if (!nuevoElem.getTipo().equals("tipo_ok")) gestorErrores(ERR_SE,4);
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
				gestorErrores(ERR_SE,4);

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
				gestorErrores(ERR_SE,4);

			nuevoElem.setTipoRet("tipo_vacio");
			break;

		case 22:
			tope = pilaSem.pop();
			pilaSem.pop();
			tope2 = pilaSem.pop();
			pilaSem.pop();
			pilaSem.pop();

			if(tope2.getTipo().equals("logico"))
				nuevoElem.setTipo(tope.getTipo());
			else
				gestorErrores(ERR_SE,5);
			nuevoElem.setTipoRet(tope.getTipoRet());
			break;

		case 23:
			pilaSem.pop();
			tope1 = pilaSem.pop();
			pilaSem.pop();
			if (!tope1.getTipo().equals("tipo_error")) 
				nuevoElem.setTipo("tipo_ok");
			else 
				gestorErrores(ERR_SE,6);
			nuevoElem.setTipoRet(tope1.getTipo());
			break;

		case 24:
		case 25:
			pilaSem.pop();
			break;

		case 26:
			tope=pilaSem.pop();
			tope1=pilaSem.pop();
			System.out.println(tope.getTipoLista());
			System.out.println(tope1.getTipo());
			if(!tope1.getTipo().equals("tipo_error") && (tope.getTipoLista() != null || !tope.getTipo().equals("tipo_error"))) {
				if(tope.getTipo() != null && tope.getTipo().equals("tipo_vacio"))
					nuevoElem.setTipo(tope1.getTipo());
				else
					nuevoElem.setTipoLista(tope.getTipoLista(), tope1.getTipo());
			}
			else
				gestorErrores(ERR_SE,7);	
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
			if(!tope1.getTipo().equals("tipo_error") && (tope.getTipoLista() != null || !tope.getTipo().equals("tipo_error"))) {
				if(tope.getTipo() != null && tope.getTipo().equals("tipo_vacio"))
					nuevoElem.anadirTipoLista(tope1.getTipo());
				else
					nuevoElem.setTipoLista(tope.getTipoLista(), tope1.getTipo());
			}
			else
				gestorErrores(ERR_SE,7);
			break;

		case 30:
			tope=pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			pilaSem.pop();
			if(!tope2.getTipo().equals("tipo_error")) {
				if(!tope.getTipo().equals("tipo_error"))
					nuevoElem.setTipo(tope2.getTipo());
				else
					gestorErrores(ERR_SE,9);
			} else {
				gestorErrores(ERR_SE,8);
			}
			if(tope2.getTipoRet().equals(tope.getTipoRet()) || tope.getTipoRet().equals("tipo_vacio"))
				nuevoElem.setTipoRet(tope2.getTipoRet());

			else
				gestorErrores(ERR_SE, 10);

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
			if(tope.getTipo().equals(tope2.getTipo()) && tope.getTipo().equals("entero"))
				nuevoElem.setTipo("logico");	
			else
				gestorErrores(ERR_SE,11);	
			break;

		case 37:
			tope=pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
			break;

		case 38:
			tope=pilaSem.pop();
			pilaSem.pop();
			tope2=pilaSem.pop();
			if(tope.getTipo().equals(tope2.getTipo()) && tope2.getTipo().equals("entero"))
				nuevoElem.setTipo("entero");
			else
				gestorErrores(ERR_SE,11);

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
				gestorErrores(ERR_SE,11);
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
			boolean error = true;
			for (String t : tipos) {
				//Comprobar si la lista es realmente una lista...
				boolean condicion = tope1.getTipoLista() != null ? buscaTipoTS(tope3.getPosi()).equals(parFunc(tope1.getTipoLista(), t)) : buscaTipoTS(tope3.getPosi()).equals(parFunc(tope1.getTipo(), t));
				if (condicion){
					nuevoElem.setTipo(t);
					error = false;
					break;
				}
			}
			if (error) gestorErrores(ERR_SE,4);
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
				gestorErrores(ERR_SE, 12);
			}

			if (tope1.getTipoRet().equals("tipo_vacio")) {
				nuevoElem.setTipoRet(tope.getTipoRet());
			} else if (tope.getTipoRet().equals("tipo_vacio")) {
				nuevoElem.setTipoRet(tope1.getTipoRet());
			} else {
				gestorErrores(ERR_SE, 13);
			}
			break;

		case 48:
			tope = pilaSem.pop();
			nuevoElem.setTipo(tope.getTipo());
			nuevoElem.setTipoRet(tope.getTipoRet());
			break;

		case 49:
			nuevoElem.setTipoRet("tipo_vacio");
			break;

		case 50:
			TablaSimbolosGlobal = new TS("G");
			desplG = 0;
			TablaSimbolosActual = TablaSimbolosGlobal;
			break;

		case 51:
			zona_decl = true;
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
			TSElem tope4_posi = TablaSimbolosGlobal.get(Integer.parseInt(tope4.getPosi().substring(1)));
			//Mirar si los argumentos son realmente una lista...
			if (tope1.getTipoLista() != null) {
				tope4_posi.setTipo("funcion");
				tope4_posi.setNArgs(tope1.getTipoLista().size());
				for (int i = 0; i < tope1.getTipoLista().size(); i++) {
					tope4_posi.setTipoArgs(tope1.getTipoLista().get(i), i);
				}
			} else {
				tope4_posi.setTipo("funcion");
				tope4_posi.setNArgs(1);
				tope4_posi.setTipoArgs(tope1.getTipo(), 0);
			}
			tope4_posi.setDesplazamiento(-1);
			tope4_posi.setEtiq("" + tope4_posi.getLexema() + "" + tope4.getPosi().substring(1));
			tope4_posi.setTipoDevuelto(tope5.getTipo());
			zona_decl = false;
			pilaSem.push(tope5);
			pilaSem.push(tope4);
			pilaSem.push(tope3);
			pilaSem.push(tope2);
			pilaSem.push(tope1);
			pilaSem.push(tope);
			break;

		case 55:
		case 56:
			tope = pilaSem.pop();
			tope1 = pilaSem.pop();
			TablaSimbolosActual.get(Integer.parseInt(tope.getPosi().substring(1))).setTipo(tope1.getTipo());
			TablaSimbolosActual.get(Integer.parseInt(tope.getPosi().substring(1))).setDesplazamiento(desplL);
			desplL += tope1.getTamano();
			pilaSem.push(tope1);
			pilaSem.push(tope);
			break;

		case 57:
			zona_decl = false;
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
			bw = new BufferedWriter(new FileWriter("Tokens.txt"));
			bwTS = new BufferedWriter(new FileWriter("TablaSimbolos.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pilaSt.push(-1);
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
				parse += (numRegla+1) + " ";
				accionSemantica(numRegla); // <-----
			} else if (accion.charAt(0) == 'a') {
				//FIN, pero si pongo return no llega a escribirTablaSimbolos
				break;
			}
		}

		bwSt = null;
		try {
			bwSt = new BufferedWriter(new FileWriter("ParseDer.txt"));
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

/* BUCLE CON PRINTS PARA DEBUGGEAR !
System.out.println("Token 1: " + token.getCodToken());
int n=0;

while(true) {
	n++;
	int s = pilaSt.peek();
	String accion = TDecLR.accion(s, token);

	System.out.println("----\nIter: " + n + " Estado: " + s + " Token: " + token.getCodToken() + " Accion: " + accion);

	if (accion == "") {
		errorSintactico(s);
		break;
	} else if (accion.charAt(0) == 'd') {
		pilaSt.push(token2int(token));
		pilaSt.push(Integer.parseInt(accion.substring(1)));

		System.out.println("Desplazamiento\nPushea (tb en semantica): " + token.getCodToken() + 
				" \nPushea Estado: " + Integer.parseInt(accion.substring(1)));

		meterEnPilaSemanticaToken(token2int(token), token); // <-----
		token = ALex();

		System.out.println("Pilla token: " +  token.getCodToken());

	} else if (accion.charAt(0) == 'r') {
		int numRegla = Integer.parseInt(accion.substring(1));

		System.out.println("Reduccion\nnRegla: " +  numRegla + " , de longitud: " + gramatica.getLongitud(numRegla));

		for (int i = 0; i < 2*gramatica.getLongitud(numRegla); i++) {
			pilaSt.pop();
		}
		int s2 = pilaSt.peek();

		System.out.println("Estado en cima: " +  s2);

		pilaSt.push(noTerm2int(gramatica.getAntecedente(numRegla)));

		System.out.println("Pushea: " +  gramatica.getAntecedente(numRegla));

		int nuevoEstado = TDecLR.goto_(s2, gramatica.getAntecedente(numRegla));
		pilaSt.push(nuevoEstado);

		System.out.println("Pushea: " +  nuevoEstado);

		parse += (numRegla+1) + " ";

		accionSemantica(numRegla); // <-----
	} else if (accion.charAt(0) == 'a') {
		//FIN, pero si pongo return no llega a escribirTablaSimbolos
		break;
	}
}*/