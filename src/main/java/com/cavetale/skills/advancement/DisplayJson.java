package com.cavetale.skills.advancement;

import lombok.Data;

@Data @SuppressWarnings("MemberName")
final class DisplayJson {
    protected IconJson icon = new IconJson();
    protected String title;
    protected String description;
    protected String background;
    protected boolean hidden;
    protected boolean announce_to_chat;
    protected boolean show_toast;
    protected String frame;
}
