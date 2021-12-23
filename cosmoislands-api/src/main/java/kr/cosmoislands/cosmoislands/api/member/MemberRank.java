package kr.cosmoislands.cosmoislands.api.member;

import lombok.Getter;

/**
 * 섬 유저 등급
 */
public enum MemberRank {
    OWNER(3), MEMBER(2), INTERN(1), NONE(0);

    @Getter
    final int priority;

    MemberRank(int i){
        this.priority = i;
    }

    public static MemberRank get(int i){
        if(i < 0 || i > 4){
            throw new IndexOutOfBoundsException();
        }

        for (MemberRank value : values()) {
            if(value.priority == i)
                return value;
        }

        return NONE;
    }
}
