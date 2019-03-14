

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Master {
    
    int sockets;	
    int maximo_sockets;
    int porta;
    int vms_per_operation;
    private ArrayList<Job> Jobs;
    
    int[] origem;
    ServerSocket master_obj;
    Socket[] slave_obj;    
    
    String compath; //diretorio compartilhado para comunicação
    String logpath; //diretorio em que serao salvos os logs dessa execução
    String logname; //nome do log a ser usado para essa execução
    String ip;
    
    ObjectOutputStream[] oos;
    ObjectInputStream[] ois;
    
    String logTemposEspalhaRecebe; //log com os tempos inicial de enviar e receber cada tarefa
    String logTemposVerificaElasticidade; //log com os tempos da verificação da elasticidade
    String logTemposConecta; //log com os tempos de conexões de novos recursos
    String logTemposDesconecta; //log com os tempos de desconexões de recursos
    
    public Master(String arq) throws IOException{
        ArrayList<Job> jobs = this.le_arquivo(arq);
        //função de calculo inicial como apenas master calculando
        long time0 = System.currentTimeMillis();
        long timen;
        for (int i = 0; i < jobs.size(); i++){
            jobs.get(i).calcula();
            System.out.println(i + ": " + jobs.get(i).get_result());
            timen = System.currentTimeMillis();
            
            System.out.println("Time: " + (timen - time0) / 1000 + "s | Tarefa " + i + " concluída!");
        }
    }
    
    public Master(int porta, int max_sockets, int sockets, int grain, String compath, String logpath, String arquivo, String IP, String logname) throws IOException{
        master_obj = new ServerSocket(porta);
	slave_obj = new Socket[max_sockets];
	origem = new int[max_sockets];
        oos = new ObjectOutputStream[max_sockets];
        ois = new ObjectInputStream[max_sockets];
        this.porta = porta;
        this.vms_per_operation = grain;
        this.sockets = sockets;
        this.maximo_sockets = max_sockets;
        this.compath = compath;
        this.logpath = logpath;
        this.ip = IP;
        this.logname = logname;
        this.Jobs = le_arquivo(arquivo); 
        this.logTemposEspalhaRecebe = "Tarefa;T1-AntesDoEnvio;T2-AposReceberResposta\n";
        this.logTemposVerificaElasticidade = "Tarefa;T1-AntesDaElasticidade;T2-DepoisDaElasticidade\n";
        this.logTemposConecta = "Tarefa;T1-AntesDeIniciarConexoes;T2-AposConexaoDoPrimeiroEscravo;T3-AposDoSegundoConexao\n;";
        this.logTemposDesconecta = "Tarefa;T1-AntesDasDesconexoes;T2-AposDesconexoes\n";
    }

    //função que fará leitura inicial do arquivo e verificará qual tipo de arquivo se está lendo: variável ou constante
    private ArrayList<Job> le_arquivo(String arq) throws FileNotFoundException, IOException{
        ArrayList<Job> tarefas; //lista com todas as tarefas lidas do arquivo que será retornada

        BufferedReader in = new BufferedReader(new FileReader(arq));
        String primeira_linha = in.readLine();
        if (primeira_linha.equalsIgnoreCase("fixo")){
            tarefas = processa_arquivo_fixo(in);
        } else {
            tarefas = processa_arquivo_variavel(in);
        }
        in.close();

        return tarefas;        
    }
    
    //essa função irá processar o arquivo considerando que cada linha possui um job a ser somente lido
    private ArrayList<Job> processa_arquivo_fixo(BufferedReader in) throws IOException{
        ArrayList<Job> tarefas = new ArrayList(); //lista com todas as tarefas lidas do arquivo que será retornada
        Job job; //auxiliar para armazenar cada nova tarefa que será lida
        //Pattern padrao = Pattern.compile("([+-];x(\\^\\d+)*)+(;[+-];x(\\^\\d+)*)*(;[+-];\\d+)*"); //expressão regular para testar sintaxe das funções
        Pattern padrao = Pattern.compile("([+-];(\\d+;)*x(\\^\\d+)*)+(;[+-];(\\d+;\\*;)*x(\\^\\d+)*)*(;[+-];\\d+)*"); //expressão regular para testar sintaxe das funções
        Matcher pesquisa;

        //BufferedReader in = new BufferedReader(new FileReader(arq));
        String str = in.readLine();
        String[] str_split;
        while (str != null){ //enquanto ha linhas para ler
             str_split = str.split(","); //divide a linha pelas vírgulas
             //as posições 1, 2 e 3 correspondem a intervao inicial, intervalo final e subdivisões respectivamente
             //esses 3 elementos são utilizados no construtor da classe Job
             job = new Job(Integer.parseInt(str_split[1]), Integer.parseInt(str_split[2]), Double.parseDouble(str_split[3]));
             pesquisa = padrao.matcher(str_split[0]);//vamos verificar se função atende expressão regular
             if (pesquisa.matches()){ //se expressão está correta vamos adiciona-la a job
                job.add_funcao(str_split[0]); //adiciona função ao job
                tarefas.add(job); //adiciona job a lista
             } else {
                System.out.println("Função " + str_split[0] + " incorreta");
             }
             str = in.readLine();
        }

        return tarefas;
    }
    
    //essa função irá processar o arquivo considerando que o mesmo possui uma única função e uma outra função definindo o tamanho dos intervalos
    private ArrayList<Job> processa_arquivo_variavel(BufferedReader in) throws IOException{
        ArrayList<Job> tarefas = new ArrayList(); //lista com todas as tarefas lidas do arquivo que será retornada
        Job job; //auxiliar para armazenar cada nova tarefa que será lida
        String[] str_split; //auxiliar
        String str; //auxiliar
        
        String function; //funcao que sera calculada
        int interval_ini, interval_fim; //intervalo inicial e final para calculo
        
        IntervalGenerator interval = new IntervalGenerator(); //gerador de quantidade de calculos
        
        //lemos a segunda linha onde temos os seguintes parametros: [função,intervalo_ini,intervalo_fim]
        //str_split[0] = função
        //str_split[1] = intervalo_ini
        //str_split[2] = intervalo_fim
        str = in.readLine();
        str_split = str.split(","); //divide a linha pelas vírgulas
        function = str_split[0];
        interval_ini = Integer.parseInt(str_split[1]);
        interval_fim = Integer.parseInt(str_split[2]);
        
        //lemos a terceira linha onde temos os seguintes parametros: [função_a_ser_usada,quantidade_de_repeticoes,param_v,param_w,param_y,param_z]
        //str_split[0] = função_a_ser_usada
        //str_split[1] = quantidade_de_repeticoes
        //str_split[2] = param_v
        //str_split[3] = param_w
        //str_split[4] = param_y
        //str_split[5] = param_z
        str = in.readLine();
        str_split = str.split(","); //divide a linha pelas vírgulas
        int totaljobs = Integer.parseInt(str_split[1]);
        int functionid = 0;
        if(str_split[0].equalsIgnoreCase("con")){functionid = 0;}
        else if(str_split[0].equalsIgnoreCase("cre")){functionid = 1;}
        else if(str_split[0].equalsIgnoreCase("dec")){functionid = 2;}
        else if(str_split[0].equalsIgnoreCase("ond")){functionid = 3;}
        else if(str_split[0].equalsIgnoreCase("ex+")){functionid = 4;}
        else if(str_split[0].equalsIgnoreCase("ex-")){functionid = 5;}
        else if(str_split[0].equalsIgnoreCase("rd0")){functionid = 6;}
        else if(str_split[0].equalsIgnoreCase("rd1")){functionid = 7;}
        switch (functionid) {
            case 0:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    int tamanho = Integer.parseInt(str_split[2]);
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  tamanho
                              );//cria job passando tamanho constante
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo CONSTANTE");
                break;
            case 1:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateAscending(i, 
                                                          Double.parseDouble(str_split[4]), 
                                                          Integer.parseInt(str_split[5])));//cria job passando os intervalos e calculando qual vai ser a quantidade de cálculos
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo CRESCENTE");
                break;
            case 2:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateDescending(i, 
                                                          Float.parseFloat(str_split[3]), 
                                                          Double.parseDouble(str_split[4]), 
                                                          Integer.parseInt(str_split[5])));
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo DECRESCENTE");
                break;
            case 3:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateWave(i,
                                  Double.parseDouble(str_split[2]), 
                                  Float.parseFloat(str_split[3]), 
                                  Double.parseDouble(str_split[4]), 
                                  Integer.parseInt(str_split[5])));
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo ONDA");
                break;
            case 4:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateExponential(i,
                                  Double.parseDouble(str_split[2]),
                                  Float.parseFloat(str_split[3]), 
                                  Double.parseDouble(str_split[4]), 
                                  Integer.parseInt(str_split[5]),
                                  "ex+"));
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo EXPONENCIAL+");
                break;
            case 5:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateExponential(i,
                                  Double.parseDouble(str_split[2]),
                                  Float.parseFloat(str_split[3]), 
                                  Double.parseDouble(str_split[4]), 
                                  Integer.parseInt(str_split[5]),
                                  "ex-"));
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo EXPONENCIAL-");
                break;
             case 6:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateRandomAll());
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo RANDOM ALL");
                break;
             case 7:
                for (int i = 1; i <= totaljobs; i++){ //considerando a quantidade de execuções informadas vamos criar uma tarefa para cada um calculando quantos cálculos realizar para cada um conforme a função informada
                    job = new Job(interval_ini, 
                                  interval_fim, 
                                  interval.generateRandomAndConstant(i, totaljobs, Integer.parseInt(str_split[2])));
                    job.add_funcao(function); //adiciona função ao job
                    tarefas.add(job); //adiciona job a lista
                    System.out.println(i + ";" + job.get_qtde_slices());
                }
                System.out.println("Lidas " + tarefas.size() + " tarefas do tipo RANDOM/CONSTANTE");
                break;
        } 
        return tarefas;
    }
    
    public void computa() throws IOException, ClassNotFoundException{
        
        //long contagem = 0;
        //for (int cont = 0; cont < Jobs.size(); cont++){
        //    contagem = (long) (contagem + Jobs.get(cont).get_qtde_slices());
        //}
        //System.out.println("Quandidade total de equações que serão calculadas: " + contagem);
        
        String temposMestre = "Ini: " + System.currentTimeMillis() + "\n";
        
        //escrevo arquivo que iniciei para autoelastic saber
        File arquivo = new File(compath + "appstarted");
        BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(this.ip);
        escritor.close();
        
        System.out.println("Quantidade de tarefas: " + Jobs.size());
        Job job; //cada Job que será processado
        int skts = 0; //contador de sockts ativos
        double prc_qtde_slices; //quantidade das subdivisões divididas por processo
        double prc_int_inicial; //intervalo inicial do processo / calculado
        double prc_int_final; //intervalo final do processo / calculado
        int i, skts_utilizados; //usarei nos for's e while's
        
        for (int nrojob = 0; nrojob < Jobs.size(); nrojob++){
            //tarefa que será computada
            job = Jobs.get(nrojob);
            //primeiramente vamos realizar as conexões sabendo-se a quantidade de processos
            logTemposVerificaElasticidade = logTemposVerificaElasticidade + nrojob + ";" + System.currentTimeMillis(); //tempo 1: antes da elasticidade
            int newsockets = verifica_sockets(nrojob);            
            while (newsockets > skts){ 
                try {
                    arquivo = new File(compath + "serverok.txt");
                    escritor = new BufferedWriter(new FileWriter(arquivo));
                    //escritor.write(myip());
                    escritor.write(this.ip);
                    escritor.close();
                } catch (IOException ex) {Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);}
                System.out.println("Aguardando conexão " + skts);
                slave_obj[skts] = master_obj.accept();
                oos[skts] = new ObjectOutputStream(slave_obj[skts].getOutputStream());
                ois[skts] = new ObjectInputStream(slave_obj[skts].getInputStream());
                System.out.println("Conexão realizada com sucesso!");
                skts++;
                logTemposConecta = logTemposConecta + ";" + System.currentTimeMillis(); //tempo após a conexão de cada escravo
            }
            logTemposVerificaElasticidade = logTemposVerificaElasticidade + ";" + System.currentTimeMillis() + "\n"; //tempo 2: após elasticidade
            skts = newsockets;
            //inicializando leitura de parâmetros do job principal para recálculo de parâmetros
            //prc_qtde_slices = Math.ceil((job.get_qtde_slices() - 1) / skts); //divido a quantidade de fatias pela quantidade de processos e arredondo para cima, desta maneira o ultimo processo vai receber apenas o resto das fatias
            prc_qtde_slices = Math.ceil(job.get_qtde_slices() / skts); //divido a quantidade de fatias pela quantidade de processos e arredondo para cima, desta maneira o ultimo processo vai receber apenas o resto das fatias
            prc_int_final = job.get_X0(); //apenas para inicialização...dentro do while sera setado este valor como inicial e o final será recalculado
            //vamos enviar para cada processo sua parte a ser processada
            skts_utilizados = 0;
            boolean tem_dados = true;
            //aqui vou enviar os dados para cada escravo
            logTemposEspalhaRecebe = logTemposEspalhaRecebe + nrojob + ";" + System.currentTimeMillis(); //tempo 1: antes do envio para os escravos
            while (tem_dados) {
                //calculo os limites
                prc_int_inicial = prc_int_final; //após isso reajusto o intervalo final e inicial a serem enviados para a próxima tarefa. aqui defino o próximo intervalo inicial como sendo o atual intervalo final
                prc_int_final = prc_int_inicial + (prc_qtde_slices * job.get_slice());
                //verifico se é meu último envio
                if (((skts_utilizados + 1) * prc_qtde_slices) >= job.get_qtde_slices()){ //se o socket atual que estou utilizando(por isso o +1) vezes a quantidade de sclices por processo/socket for maior ou igual ao total de slices, então quer dizer que este é meu último processo...ele irá receber os slices restantes ou nenhum
                    prc_qtde_slices = job.get_qtde_slices() - (skts_utilizados * prc_qtde_slices); //recalculo da quantidade de slices que ainda faltam enviar...pode ser 0 ou um valor maior que 0 e menor que o valor anterior
                    prc_int_final = prc_int_inicial + (prc_qtde_slices * job.get_slice()); //refaço o calculo do intervalo final, tendo em vista que o valor do minha quantidade de slices por processo foi alterado
                    tem_dados = false; //aviso o laço que este é o último envio...
                }
                //atualizar o tamanho da tarefa
                String streamSize = CalculateStreamSize((int) prc_qtde_slices);
                job.set_stream_size(streamSize);
                job.set_part(prc_int_inicial, prc_int_final, (prc_qtde_slices + (prc_qtde_slices * 0.3)));
                //envio da tarefa
                oos[skts_utilizados].writeObject(job); //envio uma parte da tarefa
                skts_utilizados++; //incrementa o nro de sockets utilizados
            }
            //agora vamos aguardar cada processo retornar os calculos
            //logTemposEspalhaRecebe = logTemposEspalhaRecebe + ";" + System.currentTimeMillis(); //old tempo 2: depois do envio para os escravos e antes do recebimento
            for (i = 0; i < skts_utilizados; i++){
                job = (Job) ois[i].readObject(); //recebo um resultado de um processo
                Jobs.get(nrojob).add_resultado(job.get_result()); //adiciono resultado no job correspondente
            }
            logTemposEspalhaRecebe = logTemposEspalhaRecebe + ";" + System.currentTimeMillis() + "\n"; //tempo 2: após o recebimento (aqui já crio uma nova linha para a próxima tarefa)
            //agora vou verificar o tempo já passado para imprimir
            //timen = System.currentTimeMillis();
            //System.out.println("Time: " + (timen - time0) / 1000 + "s | Tarefa " + nrojob + " concluída!");
        }
        
        //após todas as tarefas concluídas, vamos fechar as conexões ativas
        for( i = 0; i < skts; i++){
            System.out.println("Enviando mensagem quit para socket " + i);
            oos[i].writeObject(new Job("quit"));
            oos[i].close();
            ois[i].close();
            slave_obj[i].close();
        }
        master_obj.close();
        
        //("=================LOG Tempo Mestre ===================")        
        temposMestre = temposMestre + "Fim: " + System.currentTimeMillis();
        arquivo = new File(logpath + logname + "-TempoMestre.csv");
        escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(temposMestre);
        escritor.close();
        //("====================LOG Conecta======================")
        arquivo = new File(logpath + logname + "-TemposConecta.csv");
        escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(this.logTemposConecta);
        escritor.close();
        //("==================LOG Desconecta=====================")
        arquivo = new File(logpath + logname + "-TemposDesconecta.csv");
        escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(this.logTemposDesconecta);
        escritor.close();
        //("================LOG Espalha Recebe===================")
        arquivo = new File(logpath + logname + "-TemposEspalhaRecebe.csv");
        escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(this.logTemposEspalhaRecebe);
        escritor.close();
        //("=============LOG Verifica Elasticidade===============")
        arquivo = new File(logpath + logname + "-TemposVerificaElasticidade.csv");
        escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(this.logTemposVerificaElasticidade);
        escritor.close();
    }

    //recebe quantos sockets estão disponíveis para conexão
    private int verifica_sockets(int nrojob) throws IOException{
        //>>>>>>>>>>>>>>>>>vou ver se tem algum arquivo
        File diretorio = new File(compath);
        File fList[] = diretorio.listFiles();
        for (int x = 0; x < fList.length; x++){
            String nomearquivo = fList[x].getName();
            if(nomearquivo.equalsIgnoreCase("novorecurso.txt")){
                //tem novo recurso...incremento um socket a mais
                logTemposConecta = logTemposConecta + "\n" + nrojob + ";" + System.currentTimeMillis(); //tempo quando identificado que novas conexões irão ocorrer
                fList[x].delete();
                this.sockets = this.sockets + this.vms_per_operation;
            } else if (nomearquivo.equalsIgnoreCase("poucacarga.txt")){                
                logTemposDesconecta = logTemposDesconecta + nrojob + ";" + System.currentTimeMillis(); //tempo 1: antes de iniciar as desconexões
                fList[x].delete();
                Job job = new Job("quit"); //vou enviar um quit para os dois ultimos processos
                for (int i = 0; i < this.vms_per_operation; i++){
                    this.sockets--;
                    oos[this.sockets].writeObject(job);
                    oos[this.sockets].close();
                    ois[this.sockets].close();
                    slave_obj[this.sockets].close();
                }                
                File arquivo = new File(compath);
                arquivo = new File(compath + "liberarecurso.txt");
                BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivo));
                //escritor.write(myip());
                escritor.write("libera_recurso");
                escritor.close();
                logTemposDesconecta = logTemposDesconecta + ";" + System.currentTimeMillis() + "\n"; //tempo 2: após desconectar os escravos
                //try {
                    //BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivo));
                    //String ip = slave_obj[this.sockets].getInetAddress().getHostAddress();
                    //escritor.write(ip);
                    //escritor.close();
                //} catch (IOException ex) {Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);}
            }
        }
        return this.sockets;
	//le diretorio one
	// is nao tem nada entao retorna sockets
	// se tem dados para aumentar entao aumenta o numero de sockets
	// se tem aviso para diminior, entao diminui o numero de sockets, e avisa o middleware que pode fazer consolidacao.
    }
    
    private static String myip(){
        NetworkInterface iface;
	String ethr;
	String myip = "";
	String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +	"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        String regexipv4 = "^(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)$";
	try{
            for(Enumeration ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();){
                iface = (NetworkInterface)ifaces.nextElement();
		ethr = iface.getDisplayName();
		if (Pattern.matches("local", ethr)){
                    InetAddress ia;
                    for(Enumeration ips = iface.getInetAddresses();ips.hasMoreElements();){
                        ia = (InetAddress)ips.nextElement();
                        if (Pattern.matches(regexipv4, ia.getHostAddress())){
                            myip = ia.getHostAddress();
                            return myip;
			}
                    }
		}
            }
	}
	catch (SocketException e){}
	return myip;
    }
    
    public void resultados(){
        for (int i = 0; i < this.Jobs.size(); i++){
            System.out.println("Resultado f" + i + ": " + Jobs.get(i).get_result());
        }
    }

    private String CalculateStreamSize(int sizeProcessingGrainSlices) {
        // FAZER X 1 PARA DAR CERTO O TEMPO DE BUSCAR OS BYTES E PROCESSAR TAL TAREFA
        return String.valueOf(sizeProcessingGrainSlices * 1);
    }
}
