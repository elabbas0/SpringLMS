import DatePicker from "react-datepicker";

function toDate(value) {
  if (!value) return null;

  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function toIsoDate(value) {
  if (!value) return "";

  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export default function AppDateField({ value, onChange, maxDate = new Date(), placeholder = "Select date" }) {
  return (
    <DatePicker
      selected={toDate(value)}
      onChange={(selectedDate) => onChange(toIsoDate(selectedDate))}
      maxDate={maxDate}
      showMonthDropdown
      showYearDropdown
      dropdownMode="select"
      yearDropdownItemNumber={80}
      scrollableYearDropdown
      dateFormat="yyyy-MM-dd"
      placeholderText={placeholder}
      className="appDateInput"
    />
  );
}
