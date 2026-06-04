package ru.practicum.dto.user;

public record UserDto(
        String email,
        Long id,
        String name
) {
    public UserShortDto toShortDto() {
        return new UserShortDto(
                this.id,
                this.name
        );
    }
}