package ru.practicum.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ToString
@EqualsAndHashCode
public class OffsetBasedPageable implements Pageable {

    private final int offset;
    private final int size;
    private final Sort sort;

    public OffsetBasedPageable(int offset, int size, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero!");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Limit must not be less than one!");
        }
        this.offset = offset;
        this.size = size;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    public OffsetBasedPageable(int offset, int size) {
        this(offset, size, Sort.unsorted());
    }

    @Override
    public int getPageNumber() {
        return offset / size;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageable(offset + size, size, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageable(0, size, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        int newOffset = pageNumber * size;
        return new OffsetBasedPageable(newOffset, size, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset >= size;
    }

    public Pageable previous() {
        return hasPrevious() ? new OffsetBasedPageable(offset - size, size, sort) : this;
    }
}
