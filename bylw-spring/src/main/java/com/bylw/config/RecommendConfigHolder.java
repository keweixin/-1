package com.bylw.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RecommendConfigHolder {
    private final AtomicReference<Double> alpha = new AtomicReference<>(0.6);
    private final AtomicReference<Double> beta = new AtomicReference<>(0.4);
    private final AtomicReference<Double> lambda = new AtomicReference<>(0.1);
    private final AtomicReference<Integer> behaviorWindowDays = new AtomicReference<>(30);

    public Double getAlpha() { return alpha.get(); }
    public void setAlpha(Double value) { alpha.set(value); }
    public Double getBeta() { return beta.get(); }
    public void setBeta(Double value) { beta.set(value); }
    public Double getLambda() { return lambda.get(); }
    public void setLambda(Double value) { lambda.set(value); }
    public Integer getBehaviorWindowDays() { return behaviorWindowDays.get(); }
    public void setBehaviorWindowDays(Integer value) { behaviorWindowDays.set(value); }

    /** 原子更新所有参数，避免中间态被算法读取 */
    public synchronized void updateConfig(ConfigDTO config) {
        if (config.alpha() != null) this.alpha.set(config.alpha());
        if (config.beta() != null) this.beta.set(config.beta());
        if (config.lambda() != null) this.lambda.set(config.lambda());
        if (config.behaviorWindowDays() != null) this.behaviorWindowDays.set(config.behaviorWindowDays());
    }

    public record ConfigDTO(Double alpha, Double beta, Double lambda, Integer behaviorWindowDays) {}
}
