import java.util.*;
public class TxHandler {
	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
    private UTXOPool utxoPool;
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS
		this.utxoPool = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input alues is greater than or equal to the sum of
	        its output values;
	   and false otherwise.
	 */

    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        double totalInput = 0.0, totalOutput = 0.0;
        ArrayList<UTXO> pool = new ArrayList<>();
        for (int i = 0; i<tx.numInputs(); i++) {
            Transaction.Input x = tx.getInput(i);
            UTXO utxo = new UTXO(x.prevTxHash, x.outputIndex);
            //(1) all outputs claimed by tx are in the current UTXO pool
            if(!utxoPool.contains(utxo)) {
                return false;
            }
            //(2) the signatures on each input of tx are valid
            Transaction.Output y = utxoPool.getTxOutput(utxo);
            if(!y.address.verifySignature(tx.getRawDataToSign(i), x.signature)) {
                return false;
            }
            //(3) no UTXO is claimed multiple times by tx
            if (pool.contains(utxo)) {
                return false;
            }
            pool.add(utxo);
            totalInput += y.value;
            //totalInput += tx.getOutput(x).value;
        }
        
        //(4) all of tx’s output values are non-negative
        for (int i = 0; i<tx.numOutputs(); i++) {
            Transaction.Output y = tx.getOutput(i);
            if (y.value < 0){
                return false;
            } else {
                totalOutput+=y.value;
            }
        }
        
        //(5) the sum of tx’s input values is greater than or equal to the sum of
        //its output values;
        if (totalInput < totalOutput) {
            return false;
        }
        return true;
    }
	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
	    // IMPLEMENT THIS
	    ArrayList<Transaction> currValidTxs = new ArrayList<>();
	    ArrayList<Transaction> txsContainer = new ArrayList(Arrays.asList(possibleTxs));

	    for (int i=0; i < txsContainer.size(); i++) {
	        if (isValidTx(txsContainer.get(i))) {
	            currValidTxs.add(txsContainer.get(i));

                for (int j = 0; j < txsContainer.get(i).getInputs().size(); j++) {
                    UTXO tempUTXO = new UTXO(txsContainer.get(i).getInput(j).prevTxHash, txsContainer.get(i).getInput(j).outputIndex);
                    utxoPool.removeUTXO(tempUTXO);
                }
                for (int j = 0; j < txsContainer.get(i).getOutputs().size(); j++) {
                    UTXO tempUTXO = new UTXO(txsContainer.get(i).getHash(), j);
                    utxoPool.addUTXO(tempUTXO, txsContainer.get(i).getOutput(j));
                }
            }
	    }

	    Transaction[] finValidTxs = new Transaction[currValidTxs.size()];
	    for (int i = 0; i < currValidTxs.size(); i++)
	        finValidTxs[i] = currValidTxs.get(i);

	    return finValidTxs;
    }

} 
