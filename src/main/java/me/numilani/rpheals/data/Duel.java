package me.numilani.rpheals.data;

import lombok.Data;

import java.util.Objects;

@Data
public class Duel {
    public int Id;

    public String PlayerOneId;
    public int PlayerOneHealth;
    public boolean PlayerOneConfirm;

    public String PlayerTwoId;
    public int PlayerTwoHealth;
    public boolean PlayerTwoConfirm;

    public int getHealth(String uuid){
        if (Objects.equals(uuid, PlayerOneId)) return PlayerOneHealth;
        else return PlayerTwoHealth;
    }
}
