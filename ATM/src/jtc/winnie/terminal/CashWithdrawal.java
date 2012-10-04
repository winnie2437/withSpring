package jtc.winnie.terminal;

import java.util.List;

import jtc.winnie.monetary.Currency;
import jtc.winnie.terminal.AtmException;
import jtc.winnie.terminal.CashOperationException;
import jtc.winnie.terminal.CashSlice;
import jtc.winnie.monetary.Currency;



public interface CashWithdrawal 
{
	boolean withdrawalTransaction(Currency vCurrency, int vAmount, List<CashSlice> pBundle) throws CashOperationException, AtmException;	
}

