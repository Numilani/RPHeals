package me.numilani.rpheals.data;

import lombok.Data;

@Data
public class Duel {
    public int Id;

    public String PlayerOneId;
    public int PlayerOneHealth;
    public boolean PlayerOneConfirm;

    public String PlayerTwoId;
    public int PlayerTwoHealth;
    public boolean PlayerTwoConfirm;
}
