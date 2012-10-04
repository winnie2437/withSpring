package jtc.winnie.mvcatm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jtc.winnie.monetary.Currency;
import jtc.winnie.monetary.Nominal;
import jtc.winnie.terminal.AtmCashIn;
import jtc.winnie.terminal.AtmException;
import jtc.winnie.terminal.CashOperationException;
import jtc.winnie.terminal.CashSlice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class AtmMvcController
{
    private Currency getCurrency(HttpServletRequest vRequest)
    {
        Currency lCurrency = null;
        try 
        {
            lCurrency = Currency.valueOf(vRequest.getParameter("currency"));
        }
        catch (Exception e) 
        {
            ((List<String>)vRequest.getSession().getAttribute("OUTPUT")).add("unknown currency, please try again");
        }
        return lCurrency;
    }
    
    
    private AtmCashIn theATM;

    @Autowired    
    public AtmMvcController(AtmCashIn theATM)
    {
        this.theATM = theATM;
    }
    
    
    //@RequestMapping({"/","/atm"})
    @RequestMapping(value = "/atm", method = RequestMethod.GET)    
    public String firstPoint(Map<String, Object> model) 
    {
        //System.out.println(theATM.getRejectBoxState());
        return "interface";
    }    

    @RequestMapping(value = "/atm", method = RequestMethod.POST)    
    protected String postRouter(HttpSession lSession, HttpServletRequest pRequest)   
    {
        String lCard = (String)lSession.getAttribute("CARD");
        
        if (lCard == null)
        {
            return initialLoop(lSession,pRequest);
        }
        else
        {
            return clientLoop(lSession,pRequest);
        }
    }    
    
    protected String initialLoop(HttpSession lSession, HttpServletRequest pRequest)   
    {
        List<String> lOutput = (List<String>)lSession.getAttribute("OUTPUT");
        if (lOutput == null)
        {
            lOutput = new ArrayList<String>();          
            lSession.setAttribute("OUTPUT", lOutput);
        }

        String lCommand = pRequest.getParameter("command");
        
        switch (lCommand)
        {
            case "cardin":
                if (theATM.getCardReader().cardIn(pRequest.getParameter("cardnumber"),pRequest.getParameter("pin")))
                {
                    lSession.setAttribute("CARD", pRequest.getParameter("cardnumber"));
                    lOutput.add("CARD ACCEPTED");
                    return "client_interface";             
                }
                else lOutput.add("CARD REJECTED");  
                break;
                
            case "balances":
                List<CashSlice> lBalances = theATM.getAtmBalances();
                if (lBalances == null)
                {
                    lOutput.add("ATM is in unloaded state");
                }
                else 
                {
                    for (CashSlice cs : lBalances)  lOutput.add(String.format("%s %d %d",cs.getCurrency(),cs.getNominalValue(),cs.getQty()));
                }
                lOutput.add("REJECT: " + theATM.getRejectBoxState());
                lOutput.add("BALANCES OK");
                break;
                
            default: lOutput.add("unknown or wrong command, please try again");                 
                
        }
        
        return "interface"; 
    }
    

    protected String clientLoop(HttpSession lSession, HttpServletRequest pRequest)  
    {
        List<String> lOutput = (List<String>)lSession.getAttribute("OUTPUT");

        String lCommand = pRequest.getParameter("command");
        Currency lCurrency = null;
        
        switch (lCommand)
        {
            case "register":
                if ( (lCurrency = getCurrency(pRequest)) == null ) break;
                lOutput.add(theATM.registerAccount(lCurrency));
                break;
                
            case "delete":
                int lAccountToDelete = 0;   
                try 
                {
                    lAccountToDelete = Integer.parseInt(pRequest.getParameter("account"));
                }
                catch (Exception e) 
                {
                    lOutput.add("incorrect account number, please try again");
                    break;
                }
                
                lOutput.add(theATM.deleteAccount(lAccountToDelete));
                break;
                
            case "transfer":
                int lAccountFrom = 0;   
                try 
                {
                    lAccountFrom = Integer.parseInt(pRequest.getParameter("accountfrom"));
                }
                catch (Exception e) 
                {
                    lOutput.add("incorrect debit account number, please try again");
                    break;
                }

                int lAccountTo = 0; 
                try 
                {
                    lAccountTo = Integer.parseInt(pRequest.getParameter("accountto"));
                }
                catch (Exception e) 
                {
                    lOutput.add("incorrect credit account number, please try again");
                    break;
                }
                
                int lAmountForTransfer = 0;
                try 
                {
                    lAmountForTransfer = Math.round(Float.parseFloat(pRequest.getParameter("amount")) * 100);
                }
                catch (Exception e) 
                {
                    lOutput.add("incorrect amount for transfer, please try again");
                    break;
                }
                lOutput.add(theATM.moneyTransfer(lAccountFrom,lAccountTo,lAmountForTransfer));
                break;
                
            case "minus":
                if ( (lCurrency = getCurrency(pRequest)) == null ) break;
                int lAmount = 0;
                try 
                {
                    lAmount = Integer.parseInt(pRequest.getParameter("amount"));
                }
                catch (Exception e) 
                {
                    lOutput.add("incorrect amount for withdrawal, please try again");
                    break;
                }
                
                List<CashSlice> lBundle = new ArrayList<CashSlice>();               
                try
                {
                    if (!theATM.withdrawalTransaction(lCurrency,lAmount,lBundle)) break;                  
                }
                catch (CashOperationException e)
                {
                    lOutput.add("ERROR : " + e.getMessage());                   
                    break;
                }
                catch (AtmException a)
                {
                    lOutput.add("ATM_ERROR : " + a.getMessage());                   
                    break;
                }
                
                for (CashSlice cs : lBundle) lOutput.add(String.format("%d %d",cs.getNominalValue(),cs.getQty()));
                lOutput.add("WITHDRAWAL OK");
                if (!theATM.getCardReader().isAuthorized())
                {
                    lSession.setAttribute("CARD", null);                    
                    lOutput.add("CARD OUT");
                    return "interface";                
                }
                break;
                
            case "plus":
                if ( (lCurrency = getCurrency(pRequest)) == null ) break;
                Nominal lNominal = null;
                try 
                {
                    lNominal = Nominal.enumOf(Integer.parseInt(pRequest.getParameter("nominal")));
                }
                catch (Exception e) 
                {
                    lOutput.add("unknown banknote, please try again");
                    break;                  
                }

                int lQty = 0;
                try 
                {
                    lQty = Integer.parseInt(pRequest.getParameter("qty"));
                }
                catch (Exception e) 
                {
                    lOutput.add("incorrect qty for deposit, please try again");
                    break;                  
                }
                
                CashSlice lSlice = new CashSlice(lCurrency,lNominal,lQty);
                try
                {
                    if (!theATM.depositTransaction(lSlice))
                    {
                        lOutput.add("REJECTED: " + lCurrency + " "  + lNominal + " " + lQty);
                        break;                          
                    }
                }
                catch (CashOperationException e)
                {
                    lOutput.add("ERROR : " + e.getMessage());                   
                    break;
                }
                catch (AtmException a)
                {
                    lOutput.add("ATM_ERROR : " + a.getMessage());                   
                    break;
                }
                
                lOutput.add("DEPOSIT OK");
                if (!theATM.getCardReader().isAuthorized())
                {
                    lSession.setAttribute("CARD", null);                    
                    lOutput.add("CARD OUT");
                    return "interface";                
                }
                break;

            case "balance":
                if (theATM.getAccountAuthorized() == null)
                {
                    lOutput.add("client not authenticated");
                    break;
                }
                lOutput.add(String.format("CARD BALANCE: %s %10.2f",theATM.getAccountAuthorized().currency,((double)theATM.getAccountAuthorized().balance)/100));
                break;

            case "accounts":
                List<String> lAccountsInfo = theATM.getAccountsInfoList();
                if (lAccountsInfo == null)
                {
                    lOutput.add("no accounts");
                    break;
                }
                for (String str : lAccountsInfo) lOutput.add(str);
                break;
                
            case "cardtransactions":
                List<String> lTransactions = theATM.getCardTransactions();
                if (lTransactions == null)
                {
                    lOutput.add("empty list");
                    break;
                }
                for (String str : lTransactions) lOutput.add(str);
                break;

            case "clienttransactions":
                List<String> lAllTransactions = theATM.getClientTransactions();
                if (lAllTransactions == null)
                {
                    lOutput.add("empty list");
                    break;
                }
                for (String str : lAllTransactions) lOutput.add(str);
                break;
                
            case "cancel":
                //if (lATM.getCardReader().isAuthorized())
                {
                    lSession.setAttribute("CARD", null);                    
                    theATM.getCardReader().cardOut();
                    lOutput.add("CARD OUT");
                    return "interface";                
                }
                //break;                
                
            default: lOutput.add("unknown command, please try again");              
        }
        
        return "client_interface"; 
    }
    
    
}
