package jtc.winnie.terminal;

import jtc.winnie.terminal.AtmException;
import jtc.winnie.terminal.CashOperationException;
import jtc.winnie.terminal.CashSlice;

public interface CashDeposit 
{
	boolean depositTransaction(CashSlice vSlice) throws CashOperationException, AtmException;
}
