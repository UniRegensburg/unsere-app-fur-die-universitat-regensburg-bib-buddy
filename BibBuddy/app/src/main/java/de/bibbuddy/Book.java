package de.bibbuddy;

public class Book {
    
    private Long id;
    private String isbn;
    private String title;
    private String subttle;
    private Integer pubYear;
    private String publisher;
    private String volume;
    private String edition;
    private String addInfos;
    private Integer createDate;
    private Integer modDate;


    public Book(Long id, String isbn, String title, String subttle, Integer pubYear,
                String publisher, String volume, String edition, String addInfos,
                Integer createDate, Integer modDate) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.subttle = subttle;
        this.pubYear = pubYear;
        this.publisher = publisher;
        this.volume = volume;
        this.edition = edition;
        this.addInfos = addInfos;
        this.createDate = createDate;
        this.modDate = modDate;
    }

    public Book() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubttle() {
        return subttle;
    }

    public void setSubttle(String subttle) {
        this.subttle = subttle;
    }

    public Integer getPubYear() {
        return pubYear;
    }

    public void setPubYear(Integer pubYear) {
        this.pubYear = pubYear;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getAddInfos() {
        return addInfos;
    }

    public void setAddInfos(String addInfos) {
        this.addInfos = addInfos;
    }

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
    }

    public Integer getModDate() {
        return modDate;
    }

    public void setModDate(Integer modDate) {
        this.modDate = modDate;
    }

}
