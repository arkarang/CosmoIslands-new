package kr.cosmoisland.cosmoislands.bukkit.gui.components;

import com.minepalm.arkarangutils.bukkit.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;


@Getter
@RequiredArgsConstructor
public abstract class ConfirmCompounds {

    final String title;
    final Pair<String, List<String>> confirm, reject;

    public abstract void onConfirm(Player player);

    public abstract void onReject(Player player);
}
