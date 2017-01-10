import java.util.*;
import java.io.File;

class Kenken{


    static class Point{   //one point in matrix
        int x; 
        int y;
        Point(int a, int b)
        {  
            x = a; y = b;
        }
        void output(){
            System.out.println("Point("+x+","+y+")");
        }
    }

    static class Piece{  //one computation piece in Kenken 
        Op op;
        int  num;
        ArrayList<Point> points;
        Piece(Op o, int n, ArrayList<Point> p)
        {
            op = o; num = n; points = p;
        }	
        void output(){
            System.out.println("Piece: "+op+","+num+",");
            for(Point p : points){
                p.output();
            }
        }
    }

    static int dim;  //dimension of the matrix
    enum Op{ ADD, SUB, MUL, DIV, NOP}  //operator in one piece
    static Vector<Piece> problem; //a Kenken prolem, consisting of computation pieces


    static void solcopy(int[][] s, int[][] t){
        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++)
            t[i][j] = s[i][j];
        }

    }

    static HashSet<int[][]> all; //all solutions 
    static int num_sol = 0; //number of solutions

    static int[][] cand;  //the current solution candidate under trying, which is a matrix 

    static class States{    
        int[] avails;       //array recording the available values that can be used by the point
        int[] col_avails;   //array recording the used values in the same column of ths point
        int[] row_avails;   //array recording the used values in the same row of ths point
        boolean inited;
        void output(){
            System.out.println("    row avail:" + Arrays.toString(row_avails));
            System.out.println("    col avail:" + Arrays.toString(col_avails));
            System.out.println("    avail val:" + Arrays.toString(avails));
        }
    }
    static States[][] states;
    static int[] init_vals;
    
    static void init_states()
    {
        states = new States[dim][dim];
        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                states[i][j] = new States();
            }
        }

        init_vals = new int[dim];
        for(int i=0; i<dim; i++){  //initialize the default available values (i.e., all of them)
            init_vals[i] = 1;
        }
        
    }

    static void output_states()
    {
        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                System.out.println("Point:" + i + ":" + j);
                states[i][j].output();
            }
        }        
        
    }
    
    static void find_all(int s, int t) 
    {    
        //        System.out.println("Computing point: (" + s + "," + t + ")  before");
        //        output_states();
        
        if( states[s][t].inited == false ){ //only initialize when compute the same point first time
            
            states[s][t].avails = new int[dim];
            states[s][t].col_avails = new int[dim];
            states[s][t].row_avails = new int[dim];
            states[s][t].inited = true;
        }
        
        int[] avail = states[s][t].avails;
        int[] col_avail = states[s][t].col_avails;
        int[] row_avail = states[s][t].row_avails;
        
        int[] last_col_avail, last_row_avail;
        last_col_avail = last_row_avail = init_vals;  //by default, all values are available
        if(s != 0)  last_col_avail = states[s-1][t].col_avails; //not first row, inherit col_avails from point of (last_row, same_col)       
        if(t != 0)  last_row_avail = states[s][t-1].row_avails; //not first col, inherit row_avails from point of (same_row, last_col)
        
        //only the values avaliable to both column and row directions are avaliable to point (s, t)
        for(int k=0; k<dim; k++){
            avail[k] = 0;  //clear it (may save value in last compute of the same point
            if( last_row_avail[k]==1 && last_col_avail[k]==1 ){
                avail[k] = 1;  //value k+1 is available (setting element k to 1)
            }
            
            col_avail[k] = last_col_avail[k]; //inherit
            row_avail[k] = last_row_avail[k];
        }

        for(int i=0; i<dim; i++){
            if (avail[i] == 0 ) continue;
            cand[s][t] = i+1;   //fill point(s,t) with a valid value i

            if( problem!= null && matchset[s][t].exist ){ //enable a piece to match against
                boolean ok = match_piece(cand, matchset[s][t].piece);
                if(!ok){ //does not match, give up this value
                    cand[s][t] = 0; continue;
                }
            }
            
            col_avail[i] = 0;   //mark value i as unavailable
            row_avail[i] = 0;
            
            if( t+1 == dim && s+1 != dim){
                find_all(s+1, 0);  //last column while not last row, wrap around to continue next row
            }else if(t+1 != dim){	
                find_all(s, t+1);   //not last column, continue in next column
            }else{                   //(s,t) is in last column and last row. finish this solution finding
                //once come to this branch, the function will return
                int[][] sol = new int[dim][dim];
                solcopy(cand, sol); //copy the solution
                num_sol++;
                output_one(sol);
                if(problem!=null){  
                    all.add(sol);
                }
            }

            cand[s][t] = 0; //restore zero
            col_avail[i] = 1;  //restore the available value used by last iteration
            row_avail[i] = 1;
        }
    }

    static void output_one(int[][] one){

        System.out.printf("\n\nFound a solution: %dx%d\n", dim, dim);
        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                System.out.printf("%2d ", one[i][j]);	
            }
            System.out.println(" ");
        } 
    }

    static void init_one(int[][] one){

        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                one[i][j] = 0;	
            }
        } 
    }

    static void output_all(HashSet<int[][]> all){
        System.out.println("\n\n=============Total solution #:" + all.size() );
        Iterator iter = all.iterator();
        while(iter.hasNext()){
            int[][] one = (int[][])iter.next();
            output_one(one);
        }
    }

    static boolean match_add(int[][] sol, Piece quest){
        int sum = 0;
        for(Point p : quest.points){
            sum += sol[p.x][p.y]; 
        }
        return (sum == quest.num);
    }

    static boolean match_sub(int[][] sol, Piece quest){
        Point p0 = quest.points.get(0);
        Point p1 = quest.points.get(1);
        
        int v1 = sol[p0.x][p0.y];
        int v2 = sol[p1.x][p1.y];

        return ((v1-v2) == quest.num) || ((v2-v1) == quest.num);
    }

    static boolean match_mul(int[][] sol, Piece quest){
        int prod = 1;
        for(Point p : quest.points){
            prod *= sol[p.x][p.y]; 
        }
        return (prod == quest.num);
    }

    static boolean match_div(int[][] sol, Piece quest){
        Point p0 = quest.points.get(0);
        Point p1 = quest.points.get(1);
        
        int v1 = sol[p0.x][p0.y];
        int v2 = sol[p1.x][p1.y];

        return ((v1/v2) == quest.num) || ((v2/v1) == quest.num);
    }

    static boolean match_nop(int[][] sol, Piece quest){
        Point p = quest.points.get(0);
        return (quest.num == sol[p.x][p.y]); 
    }

    
    static boolean match_piece(int[][] sol, Piece quest)
    {
        boolean ok = false;
        switch(quest.op){
        case ADD:
            ok = match_add(sol, quest);
            break;
        case SUB:
            ok = match_sub(sol, quest);
            break;
        case MUL:
            ok = match_mul(sol, quest);
            break;
        case DIV:
            ok = match_div(sol, quest);
            break;
        case NOP:
            ok = match_nop(sol, quest);
            break;
        default:
            assert(false);
        }
        return ok;
        
    }
    
    static boolean match_solution(int[][] sol, Vector<Piece> quests){
        
        boolean ok = false;
        for(Piece quest : quests){
            ok = match_piece(sol, quest);
            if( !ok ) return false;
        }
        return true;
    }

    public static int input(Vector<Piece> prob, File f){

        int col = 0;
        int row = 0;
        int last_col = 0;
        try {
            //read the matrix input
            Map<Character, ArrayList<Point>> map = new HashMap<Character, ArrayList<Point>>(); 
            Scanner sc = new Scanner(f);
            while(sc.hasNextLine()) { //read one row of the matrix
                String line = sc.nextLine();
                if(line.length() == 0) break; 
                System.out.println(line);
                Scanner sl = new Scanner(line).useDelimiter("\\s*");
                while(sl.hasNext()){  //read one column of the row
                    char c = sl.next().charAt(0);
                    ArrayList<Point> points = map.get(c);
                    if(points == null){ //the region has not been read yet, new the region
                        points = new ArrayList<Point>();
                    }
                    Point point = new Point(row, col);
                    points.add(point); 
                    map.put(c, points);
                    col++;
                }
                sl.close();	

                if(last_col != 0) assert(last_col == col);
                else last_col = col;
                col = 0;
                row++;
            }
            
            System.out.printf("===%dx%d===\n", row, last_col);
            assert(last_col == row);
            
            //read the region operation input 
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                System.out.println(line);
                Scanner sl = new Scanner(line).useDelimiter("\\s+");

                String a = sl.next();
                assert(a.length()==1);
                char A = a.charAt(0);

                int num = Integer.parseInt(sl.next());
                assert(num != 0);

                Op op = null;
                if(!sl.hasNext()) 
                op = Op.NOP;
                else{
                    String o = sl.next();
                    assert(o.length()==1);
                    char O = o.charAt(0);
                    switch(O){
                    case '+': op = Op.ADD; break;
                    case '-': op = Op.SUB; break;
                    case '*': op = Op.MUL; break;
                    case '/': op = Op.DIV; break;
                    default: assert(false);
                    }
                }
                assert(op != null);	
                ArrayList<Point> points = map.get(A);
                assert(points!=null);
                Piece p = new Piece(op, num, points);
                //p.output();
                prob.add(p);
            }


            sc.close();
        } 
        catch (Exception e) {
            assert(false);
        }	

        return row;
    } 

    static Point find_max(ArrayList<Point> points)
    {
        Point max = new Point(0,0);
        for( Point point: points){
            if( max.x == point.x){
                if( max.y < point.y ) max.y = point.y;
            }else if ( max.x < point.x) {
                max.x = point.x;
                max.y = point.y;
            }
        } 
        return max;
    }
    
    static class Problem_at{
        boolean exist;
        Piece piece;        
        Problem_at(boolean e, Piece p)
        {
            exist = e;
            piece = p;
        }
    }
    
    static Problem_at[][] matchset;
    
    static void problem_opt(Vector<Piece> prob)
    {
        matchset = new Problem_at[dim][dim];
        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                matchset[i][j] = new Problem_at(false, null);
            }
        }
        
        for(Piece piece: prob){
            Point max_point = find_max(piece.points);
            matchset[max_point.x][max_point.y] = new Problem_at(true, piece);

            //System.out.println(max_point.x + " : " + max_point.y);
            //matchset[max_point.x][max_point.y].piece.output();
        }
        
    }
    
    public static void output_help(){	
        System.out.println("");
        System.out.println("Please input a number for Soduku or input a file for Kenken. For examples:");
        System.out.println("  - To get all the 5x5 Soduku results");
        System.out.println("         java Kenken 5");
        System.out.println("  - To solve a Kenken problem in file kenken.5");
        System.out.println("         java Kenken kenken.5");
        System.out.println("");
    }

    public static void main(String[] args)
    {
        dim = 0;
        problem = null;
        if(args.length != 0){
            File file = new File(args[0]);
            if(file.exists() && !file.isDirectory()){
                problem = new Vector<Piece>();
                dim = input(problem, file);
            }else{
                try{
                    dim = Integer.parseInt(args[0]);
                    if(dim > 15){
                        System.out.println("\nYour input number is too big.\n");
                    }
                }catch(Exception e){
                    System.out.println("\nYour input is not a number or a file name.");
                    dim = 0;
                }
            }
        }

        if(dim==0){
            output_help();
            return;
        }
        
        if(problem != null){ //find the quests that are ready to match against at each point
            problem_opt(problem);
        }

        cand = new int[dim][dim];
        all = new HashSet<int[][]>();
        
        init_states();        
        
        find_all(0, 0); //find all solutions in this recursive function starting from point (0, 0)
        
        System.out.println("\n\nTotal solution #: "+ num_sol);
    }

}

