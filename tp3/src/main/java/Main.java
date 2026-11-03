import org.chocosolver.mining.patterns.constraints.factory.ConstraintFactory;
import org.chocosolver.mining.patterns.io.TransactionFiles;
import org.chocosolver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.mining.patterns.search.strategy.selectors.variables.MinCov;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0]
                : "C:/Users/salma/Desktop/cp-td3/chocominer/chocominer-core/src/test/resources/contextPasquier99/contextPasquier99.dat";
        int alpha = args.length>1 ? Integer.parseInt(args[1]) : 1; //question 3 min frequency
        int lb = args.length > 2 ? Integer.parseInt(args[2]) : 1;   //min pattern size 
        int catSize = args.length > 3 ? Integer.parseInt(args[3]) : 2; // Q4: category size ({1,2}, {3,4}; item 5 in none)
        int m = args.length > 4 ? Integer.parseInt(args[4]) : 2;       // Q4: min number of categories    
        TransactionalDatabase database = TransactionFiles.readItems(path);

        Model model = new Model("Closed Itemset Mining");
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        model.sum(x, "=", length).post();
        ConstraintFactory.coverSize(database, freq, x).post();
        ConstraintFactory.coverClosure(database, x).post();
        // question 3: 
        model.arithm(freq, ">=", alpha).post();
        model.arithm(length,">=", lb).post();
        //question 4: 
        if(catSize > 0){
            int nbcat = database.getNbItems() / catSize; 
            BoolVar[] in_cat = model.boolVarArray("in_category", nbcat);
            for(int i=0 ; i <nbcat; i++){
                BoolVar[] block = Arrays.copyOfRange(x, i * catSize, (i+1) * catSize);
                model.max(in_cat[i], block).post();
            }
            model.sum(in_cat, ">=", m).post();
        }

        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(new MinCov(model, database), new IntDomainMin(), x));

        int count = 0;
    while (solver.solve()) {
            int[] itemset = IntStream.range(0, x.length)
                    .filter(i -> x[i].getValue() == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            System.out.println(Arrays.toString(itemset) + ", freq=" + freq.getValue());
            count++;
        }
        System.out.println(count + " closed itemsets found");
    }
}
