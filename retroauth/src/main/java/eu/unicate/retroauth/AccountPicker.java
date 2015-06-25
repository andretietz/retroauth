package eu.unicate.retroauth;


import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public final class AccountPicker {
	private AccountPicker() {
	}

	public static Intent newChooseAccountIntent(Account selectedAccount, ArrayList<Account> allowableAccounts, String[] allowableAccountTypes, boolean alwaysPromptForAccount, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
		return newChooseAccountIntent(selectedAccount, allowableAccounts, allowableAccountTypes, alwaysPromptForAccount, descriptionOverrideText, addAccountAuthTokenType, addAccountRequiredFeatures, addAccountOptions, false);
	}

	public static Intent newChooseAccountIntent(Account selectedAccount, ArrayList<Account> allowableAccounts, String[] allowableAccountTypes, boolean alwaysPromptForAccount, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions, boolean setGmsCoreAccount) {
		return newChooseAccountIntent(selectedAccount, allowableAccounts, allowableAccountTypes, alwaysPromptForAccount, descriptionOverrideText, addAccountAuthTokenType, addAccountRequiredFeatures, addAccountOptions, setGmsCoreAccount, 0, 0);
	}

	public static Intent newChooseAccountIntent(Account selectedAccount, ArrayList<Account> allowableAccounts, String[] allowableAccountTypes, boolean alwaysPromptForAccount, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions, boolean setGmsCoreAccount, int overrideTheme, int overrideCustomTheme) {
		Intent intent = new Intent();
		intent.setAction("com.google.android.gms.common.account.CHOOSE_ACCOUNT");
		intent.setPackage("com.google.android.gms");
		intent.putExtra("allowableAccounts", allowableAccounts);
		intent.putExtra("allowableAccountTypes", allowableAccountTypes);
		intent.putExtra("addAccountOptions", addAccountOptions);
		intent.putExtra("selectedAccount", selectedAccount);
		intent.putExtra("alwaysPromptForAccount", alwaysPromptForAccount);
		intent.putExtra("descriptionTextOverride", descriptionOverrideText);
		intent.putExtra("authTokenType", addAccountAuthTokenType);
		intent.putExtra("addAccountRequiredFeatures", addAccountRequiredFeatures);
		intent.putExtra("setGmsCoreAccount", setGmsCoreAccount);
		intent.putExtra("overrideTheme", overrideTheme);
		intent.putExtra("overrideCustomTheme", overrideCustomTheme);
		return intent;
	}
}
