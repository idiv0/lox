import argparse
import os.path
import sys
from typing import List

def generate_ast(output_dir: str):
    grammar = [
        "Binary     : Expr left, Token operator, Expr right",
        "Grouping   : Expr expression",
        "Literal    : Object value",
        "Unary      : Token operator, Expr right"
    ]
    define_ast(output_dir, "Expr", grammar)
    

def define_ast(output_dir: str, base_name: str, types: List[str]):
    path = os.path.join(output_dir, base_name + ".java")
    with open(path, 'w', encoding='utf-8') as writer:
        lines = ["package lox;"]
        lines.append("")
        lines.append("import java.util.List;")
        lines.append("")

        # Base class
        lines.append(f"abstract class {base_name}")
        lines.append("{")

        # Visitor interface
        lines += define_visitor(base_name, types)

        for type in types:
            class_name = type.split(":")[0].strip()
            fields = type.split(":")[1].strip()
            lines += define_type(writer, base_name, class_name, fields)

        # Base class accept() method.
        lines.append(f"{indent(4)} abstract <R> R accept(Visitor<R> visitor);")

        lines.append("}\n")

        writer.write("\n".join(lines))


def define_visitor(base_name: str, types: List[str]):
    """
    Define the visitor interface.
    
    Generate a method for each type.
    """
    lines = [indent(4) + "interface Visitor<R>"]
    lines.append(indent(4) + "{")

    for type in types:
        type_name = type.split(":")[0].strip()
        lines.append(f"{indent(8)}R visit{type_name}{base_name} ({type_name} {base_name.lower()});")

    lines.append(indent(4) + "}")
    return lines


def define_type(writer, base_name: str, class_name: str, fields: str):

    # Class definition
    lines = [f"{indent(4)}static class {class_name} extends {base_name}"]
    lines.append(indent(4) + "{")

    # Constructor
    lines.append(indent(8) + f"{class_name} ({fields})")
    lines.append(indent(8) + "{")

    # Store parameters in fields
    fields_list = fields.split(", ")
    for field in fields_list:
        name = field.split(" ")[1].strip()
        lines.append(indent(12) + f"this.{name} = {name};")
    
    lines.append(indent(8) + "}")
    # End of constructor
    
    # Visitor pattern in impl classes.
    lines.append("")
    lines.append(f"{indent(8)}@Override")
    lines.append(f"{indent(8)}<R> R accept(Visitor<R> visitor)")
    lines.append(indent(8) + "{")
    lines.append(f"{indent(12)} return visitor.visit{class_name}{base_name}(this);")
    lines.append(indent(8) + "}")

    # Fields
    lines.append("")
    for field in fields_list:
        lines.append(indent(8) + f"final {field};")

    lines.append(indent(4) + "}\n")

    return lines


def indent(num: int):
    return " " * num


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("output_directory")
    args = parser.parse_args()

    generate_ast(args.output_directory)
