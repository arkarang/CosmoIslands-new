package kr.cosmoislands.cosmoislands.bukkit.test;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tests {

    Pattern pattern = Pattern.compile("\\&[0-9a-f]|\\#[0-9a-z]{0,6}");
/*
LevelLore: '§f섬 레벨 §l[ %level% §f§l] 증가'
LevelLorePattern: '(?<=§f섬\s레벨§l\s\[\s).*(?=\s§f§l]\s증가)'
*
* */
    @Test
    public void asdf(){
        //(?<=ㅎㅇ)[0-9].*(?=4)
        String text = "§f섬 레벨 §l[ %level% §f§l] 증가";
        Pattern pattern = Pattern.compile("(?<=§f섬\\s레벨\\s§l\\[\\s).*(?=\\s§f§l]\\s증가)");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()) {
            System.out.println(matcher.group());
        }else{
            System.out.println("not found");
        }
    }
}
