package com.ssafy.interest.dto;

public class BookmarkStatusDto {
    private boolean bookmarked;

    public BookmarkStatusDto() {}

    public BookmarkStatusDto(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
