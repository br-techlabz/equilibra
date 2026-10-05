package br.com.equilibra.account.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetAccountTest {

    @Test
    void createsValidAccountWithNormalizedNameAndMoneyScale() {
        AssetAccount account = new AssetAccount(
            "owner-id",
            "  Nubank  ",
            AssetAccountType.DIGITAL,
            new BigDecimal("1250.35")
        );

        assertThat(account.getName()).isEqualTo("Nubank");
        assertThat(account.getNormalizedName()).isEqualTo("nubank");
        assertThat(account.getInitialBalance()).isEqualByComparingTo("1250.35");
        assertThat(account.getInitialBalance().scale()).isEqualTo(2);
        assertThat(account.isActive()).isTrue();
    }

    @Test
    void permitsNegativeAndZeroInitialBalance() {
        assertThat(new AssetAccount("owner", "Devedor", AssetAccountType.CHECKING, new BigDecimal("-500.00"))
            .getInitialBalance()).isEqualByComparingTo("-500.00");
        assertThat(new AssetAccount("owner", "Caixa", AssetAccountType.CASH, BigDecimal.ZERO)
            .getInitialBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsInvalidRequiredFieldsAndScale() {
        assertThatThrownBy(() -> new AssetAccount("owner", " ", AssetAccountType.CASH, BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AssetAccount("owner", "Conta", null, BigDecimal.ZERO))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AssetAccount("owner", "Conta", AssetAccountType.CASH, new BigDecimal("1.001")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void supportsLifecycleOperations() {
        AssetAccount account = new AssetAccount("owner", "Conta", AssetAccountType.CASH, BigDecimal.ZERO);

        account.deactivate();
        assertThat(account.isActive()).isFalse();

        account.activate();
        account.rename(" Nova Conta ");
        account.changeType(AssetAccountType.SAVINGS);
        account.changeInitialBalance(new BigDecimal("10.00"));

        assertThat(account.isActive()).isTrue();
        assertThat(account.getName()).isEqualTo("Nova Conta");
        assertThat(account.getNormalizedName()).isEqualTo("nova conta");
        assertThat(account.getType()).isEqualTo(AssetAccountType.SAVINGS);
    }
}
