package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by andre on 15/04/16.
 */
public class AndroidOwnerManager implements OwnerManager<Account, AndroidTokenType> {

    private final AuthAccountManager accountManager;
    private final ContextManager contextManager;

    public AndroidOwnerManager(AuthAccountManager accountManager, ContextManager contextManager) {
        this.accountManager = accountManager;
        this.contextManager = contextManager;
    }


    public Account getOwner(AndroidTokenType type) throws ChooseOwnerCanceledException {
        // get active account name
        String accountName = accountManager.getActiveAccountName(type.accountType);
        // if this one exists, try to get the account
        if (accountName != null) return accountManager.getAccountByName(type.accountType, accountName);
        // if it doesn't, ask the user to pick an account
        accountName = showAccountPickerDialog(type.accountType, true);
        // if the user has chosen an existing account
        if (accountName != null) {
            accountManager.setActiveAccount(type.accountType, accountName);
            return accountManager.getAccountByName(type.accountType, accountName);
        }
        // if the user chose to add an account, handled by the android token storage
        return null;
    }

    /**
     * Shows an account picker for the user to choose an account.
     *
     * @param accountType   Account type of the accounts the user can choose
     * @param canAddAccount if <code>true</code> the user has the option to add an account
     * @return the accounts the user chooses from
     */
    public String showAccountPickerDialog(String accountType, boolean canAddAccount) throws ChooseOwnerCanceledException {
        Account[] accounts = AccountManager.get(contextManager.getContext()).getAccountsByType(accountType);
        if (accounts.length == 0) return null;
        String[] accountList = new String[canAddAccount ? accounts.length + 1 : accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            accountList[i] = accounts[i].name;
        }
        if (canAddAccount) {
            accountList[accounts.length] = contextManager.getContext().getString(R.string.add_account_button_label);
        }
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        Activity activity = contextManager.getActivity();
        ShowDialogOnUI showDialog = new ShowDialogOnUI(accountList, lock, condition);
        if (activity != null) {
            activity.runOnUiThread(showDialog);
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                // ignore
            } finally {
                lock.unlock();
            }
        }
        if (showDialog.canceled) {
            throw new ChooseOwnerCanceledException("User canceled authentication!");
        }
        return showDialog.selectedOption;
    }

    private class ShowDialogOnUI implements Runnable {

        private final Condition condition;
        private final String[] options;
        private final Lock lock;
        String selectedOption;
        boolean canceled = false;

        ShowDialogOnUI(String[] options, Lock lock, Condition condition) {
            this.options = options;
            this.condition = condition;
            this.lock = lock;
            this.selectedOption = options[0];
        }

        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(contextManager.getActivity());
            builder.setTitle(contextManager.getContext().getString(R.string.choose_account_label));
            builder.setCancelable(false);
            builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which < options.length - 1) {
                        selectedOption = options[which];
                    } else {
                        selectedOption = null;
                    }
                }
            });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    lock.lock();
                    try {
                        condition.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    canceled = true;
                    lock.lock();
                    try {
                        condition.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            });
            builder.show();
        }
    }
}
