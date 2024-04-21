
import java.util.*;
import java.io.*;
public class Main {
    //输出数组
    public Vector<Output> writer = new Vector<>();
    public int row=1;//记录行数
    public int col=1;//记录列数
    public int ch;
    public int code;//保留字状态码
    public StringBuffer strToken = new StringBuffer();//存放构成单词符号的字符串
    public String[] RetainWords = {"int","if","else","return","for","void","while","break","then","do","printf","float","double","char","include","void","main","cout","cin","using","struct","switch","case","std","namespace"};
    public Map<String, Integer> RWmap= new HashMap<>();//保留字
    public String[] operator = {".","+", "-", "*", "/","%", ">", ">=", "<", "<=","=" ,"<>","#","(", ")",  "[", "]", "{","}",":=",":",";"};
    public Map<String,Integer> OpMap = new HashMap<>();//操作符表
    public Map<String,Integer> IdentifierMap = new HashMap<>();//标识符表
    public int error = 0;

    public static void main(String[] args) {
        Main lexer = new Main();
        for(var i = 0; i <lexer.operator.length; i++) {
            lexer.OpMap.put(lexer.operator[i],i);
        }
        for(var i = 0; i < lexer.RetainWords.length; i++) {
            lexer.RWmap.put(lexer.RetainWords[i],i);
        }
        System.out.println("请输入文件名：");
        Scanner scanner = new Scanner(System.in);
        String filename = scanner.nextLine();
        String source = lexer.filein(filename);
        char[] src = source.toCharArray();
        //词法分析
        lexer.processor(src,source);
        //输出结果
        for(Output a:lexer.writer){
            System.out.println(a.word()+'\t'+a.info()+'\t'+a.type()+'\t'+a.pos()+'\r');
        }
    }
    //将源代码从文件中输入
    public String filein(String filename){
        StringBuilder src = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null){
                src.append(line);
                src.append("\r");
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return src.toString();
    }
    //判断是否是字母
    public boolean isLetter(){
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    //判断是否是数字
    public boolean isDigit(){
        return ch >= '0' && ch <= '9';
    }

    //判断是否是空格或制表符
    public boolean isBC(){
        return ch == ' ' || ch == '\t';
    }
    //判断是否是回车键
    public boolean isENTER(){
        return ch == '\r';
    }
    //判断是否是分界符
    public boolean isOperator() {
        return OpMap.containsKey(Character.toString(ch));
    }

    //判断返回值
    public int Reserve(){
        //为保留字
        if(RWmap.containsKey(strToken.toString())){
            return 1;
        }
        //为已记录的标识符
        if(IdentifierMap.containsKey(strToken.toString())){
            return 2;
        }
        //为操作符
        if(OpMap.containsKey(strToken.toString())){
            Integer i = OpMap.get(strToken.toString());
            if(i>=12){
                return 3;
            }
            if(i>=6){
                return 4;
            }
            if(i>=1)
                return 5;
        }
        //开头为字母，并未加入标识符的标识符
        if(strToken.length()!=0){
            if(strToken.charAt(0)>='a'&&strToken.charAt(0)<='z'||strToken.charAt(0)>='A'&&strToken.charAt(0)<='Z'||strToken.charAt(0)=='_'){
                return 6;
            }
        }
        //开头为数字
        if(strToken.length()!=0){
            if(strToken.charAt(0)>='0'&&strToken.charAt(0)<='9')
                return 7;
        }
        return -1;
    }
    public String Nm(int code){
        if(code==1)
            return "保留字";
        if(code==2)
            return "标识符";
        if(code==3)
            return "分隔符";
        if(code==4)
            return "关系运算符";
        if(code==5)
            return "算术运算符";
        if(code==6){
            IdentifierMap.put(strToken.toString(),1);
            return "标识符";
        }
        if(code==7&&error!=1)
            return "常数";
        return "Error";
    }

    //记录到输出中
    public void write(){
        code = Reserve();
        String a = strToken.toString();
        String b = Nm(code);
        String c;
        if(b.equals("Error")){
            c = "Error";
            error = 0;
        }
        else{
        c = "("+ code +","+a+")";
        }
        String d = "("+row+","+col+")";
        writer.add(new Output(a,c,b,d));

    }
    public void processor(char[] src,String source){
        int i=0;
        do{
            ch = src[i];
            if(!isBC()){
                //若输入回车
                if(isENTER()){
                    if(strToken.length()!=0) {
                        write();
                    }
                    col++;
                    strToken.delete(0,strToken.length());
                    row++;
                    col=1;
                }
                //若输入字母
                else if(isLetter()){
                    if(strToken.length()!=0&&Reserve()!=6){
                        if(Reserve()==7){
                            strToken.append(src[i]);
                            error = 1;
                        }
                        else {
                            write();
                            col++;
                            strToken.delete(0, strToken.length());
                            strToken.append(src[i]);
                        }
                    }
                    else{
                        strToken.append(src[i]);
                    }
                }
                //若输入数字
                else if(isDigit()){
                    if(strToken.length()!=0&&Reserve()!=7){
                        if(Reserve()==6)
                            strToken.append(src[i]);
                        else{
                            write();
                            col++;
                            strToken.delete(0,strToken.length());
                            strToken.append(src[i]);
                        }
                    }
                    else{
                        strToken.append(src[i]);
                    }
                }
                //若输入操作符
                else if(isOperator()) {
                    //处理注释
                    if(strToken.length()>=1&&src[i-1]=='/'){
                        if(src[i]=='/'){
                            strToken.delete(strToken.length()-1,strToken.length());
                            if(strToken.length()!=0) {
                                write();
                                strToken.delete(0,strToken.length());
                            }
                            col++;
                            while(i<src.length&&src[i]!='\r'){
                                i++;
                            }
                            i--;
                        }
                        else if(src[i]=='*'){
                            strToken.delete(strToken.length()-1,strToken.length());
                            if(strToken.length()!=0) {
                                write();
                                strToken.delete(0, strToken.length());
                            }
                            col++;
                            boolean star=false;
                            boolean Y=true;
                            i++;
                            while(i<src.length&&Y){
                                if(src[i]=='*'){
                                    star = true;
                                }
                                else if(src[i]=='/'&&star){
                                    if(src[i-1]=='*')
                                        Y=false;
                                    else
                                        star=false;
                                }
                                else if(src[i]=='\r'){
                                    row++;
                                    col=1;
                                }
                                i++;
                            }
                            i--;
                        }
                    }
                    else if(strToken.length()!=0&&Reserve()!=3&&Reserve()!=4&&Reserve()!=5){
                        if(Reserve()==7&&src[i]=='.')
                            strToken.append(src[i]);
                        else{
                            write();
                            col++;
                            strToken.delete(0,strToken.length());
                            strToken.append(src[i]);
                        }
                    }
                    else if(strToken.length()!=0&&(src[i-1]==';'||src[i]==';')){
                        write();
                        strToken.delete(0,strToken.length());
                        strToken.append(src[i]);
                        col++;
                    }
                    else if(strToken.length()!=0&&(Reserve()==3||Reserve()==4||Reserve()==5)){
                        strToken.append(src[i]);
                    }
                    else if(strToken.length()!=0){
                        write();
                        strToken.delete(0,strToken.length());
                        strToken.append(src[i]);
                        col++;
                    }
                    else{
                        strToken.append(src[i]);
                    }
                }

            }
            //若为空格或制表符
            else{
                if(ch=='\r') {
                    row++;
                    col=0;
                }
                else{
                    if(strToken.length()!=0){
                        write();
                        strToken.delete(0,strToken.length());
                        col++;
                    }
                }
            }
            i++;
        }while(i<source.length());
    }
}
record Output(String word, String info, String type, String pos){}