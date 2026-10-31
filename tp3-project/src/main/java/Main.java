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
        TransactionalDatabase database = TransactionFiles.readItems(
                "C:/Users/salma/Desktop/cp-td3/chocominer/chocominer-core/src/test/resources/contextPasquier99/contextPasquier99.dat");

        Model model = new Model("Closed Itemset Mining");
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        model.sum(x, "=", length).post();
        ConstraintFactory.coverSize(database, freq, x).post();
        ConstraintFactory.coverClosure(database, x).post();

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
