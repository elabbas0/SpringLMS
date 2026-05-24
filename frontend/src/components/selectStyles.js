export const darkSelectStyles = {
  control: (base, state) => ({
    ...base,
    backgroundColor: "#181818",
    borderColor: state.isFocused ? "#777" : "#444",
    boxShadow: "none",
    minHeight: 42,
    borderRadius: 4,
    "&:hover": {
      borderColor: "#777"
    }
  }),
  menu: (base) => ({
    ...base,
    backgroundColor: "#181818",
    border: "1px solid #333",
    overflow: "hidden"
  }),
  menuList: (base) => ({
    ...base,
    paddingTop: 4,
    paddingBottom: 4
  }),
  option: (base, state) => ({
    ...base,
    backgroundColor: state.isFocused ? "#252525" : "#181818",
    color: "#eee",
    cursor: "pointer"
  }),
  singleValue: (base) => ({
    ...base,
    color: "#eee"
  }),
  input: (base) => ({
    ...base,
    color: "#eee"
  }),
  placeholder: (base) => ({
    ...base,
    color: "#888"
  }),
  indicatorSeparator: (base) => ({
    ...base,
    backgroundColor: "#444"
  }),
  dropdownIndicator: (base) => ({
    ...base,
    color: "#aaa",
    "&:hover": {
      color: "#fff"
    }
  })
};
