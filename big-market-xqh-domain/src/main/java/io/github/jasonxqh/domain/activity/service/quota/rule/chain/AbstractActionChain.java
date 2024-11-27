package io.github.jasonxqh.domain.activity.service.quota.rule.chain;


public abstract class AbstractActionChain implements IActionChain {
    private IActionChain next;

    @Override
    public IActionChain appendNext(IActionChain next) {
        this.next = next;
        return next;
    }

    @Override
    public IActionChain next() {
        return next;
    }

    protected abstract String chainModel();
}
