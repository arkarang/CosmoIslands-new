package kr.cosmoisland.cosmoislands.api;

/**
 * 섬 모듈 우선순위입니다.
 * HIGH > MEDIUM > LOW 순서로 우선순위를 가집니다.
 * 즉, HIGH가 먼저 로드되고, LOW가 나중에 로드됩니다.
 */
public enum ModulePriority {

    HIGH, MEDIUM, LOW;
}
